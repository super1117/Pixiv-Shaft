package ceui.lisa.fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.effective.android.panel.PanelSwitchHelper;

import java.util.List;

import ceui.lisa.R;
import ceui.lisa.activities.TemplateActivity;
import ceui.lisa.activities.UserActivity;
import ceui.lisa.adapters.BaseAdapter;
import ceui.lisa.adapters.CommentAdapter;
import ceui.lisa.adapters.EmojiAdapter;
import ceui.lisa.core.RemoteRepo;
import ceui.lisa.databinding.FragmentCommentBinding;
import ceui.lisa.databinding.RecyCommentListBinding;
import ceui.lisa.dialogs.SoftKeyboardStateHelper;
import ceui.lisa.http.NullCtrl;
import ceui.lisa.http.Retro;
import ceui.lisa.interfaces.OnItemClickListener;
import ceui.lisa.model.EmojiItem;
import ceui.lisa.model.ListComment;
import ceui.lisa.models.CommentHolder;
import ceui.lisa.models.CommentsBean;
import ceui.lisa.utils.Common;
import ceui.lisa.utils.Emoji;
import ceui.lisa.utils.Params;
import ceui.lisa.view.EditTextWithSelection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static ceui.lisa.activities.Shaft.sUserModel;

public class FragmentComment extends NetListFragment<FragmentCommentBinding,
        ListComment, CommentsBean> {

    private static final String[] OPTIONS = new String[]{"回复评论", "复制评论", "查看用户"};
    private int illustID;
    private String title;
    private int parentCommentID;
    private PanelSwitchHelper mHelper;
    private int selection = 0;

    public static FragmentComment newInstance(int id, String title) {
        Bundle args = new Bundle();
        args.putInt(Params.ILLUST_ID, id);
        args.putString(Params.ILLUST_TITLE, title);
        FragmentComment fragment = new FragmentComment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void initBundle(Bundle bundle) {
        illustID = bundle.getInt(Params.ILLUST_ID);
        title = bundle.getString(Params.ILLUST_TITLE);
    }

    @Override
    public void initLayout() {
        mLayoutID = R.layout.fragment_comment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mHelper == null) {
            mHelper = new PanelSwitchHelper.Builder(this)
                    .contentCanScrollOutside(false)    //可选模式，默认true，当面板实现时内容区域是否往上滑动
                    .logTrack(true)
                    //可选，默认false，是否开启log信息输出
                    .build(false);              //可选，默认false，是否默认打开输入法
        }
    }

    @Override
    public RemoteRepo<ListComment> repository() {
        return new RemoteRepo<ListComment>() {
            @Override
            public Observable<ListComment> initApi() {
                return Retro.getAppApi().getComment(sUserModel.getResponse().getAccess_token(), illustID);
            }

            @Override
            public Observable<ListComment> initNextApi() {
                return Retro.getAppApi().getNextComment(
                        sUserModel.getResponse().getAccess_token(), mModel.getNextUrl());
            }
        };
    }

    @Override
    public BaseAdapter<CommentsBean, RecyCommentListBinding> adapter() {
        return new CommentAdapter(allItems, mContext).setOnItemClickListener((v, position, viewType) -> {
            if (viewType == 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setItems(OPTIONS, (dialog, which) -> {
                    if (which == 0) {
                        baseBind.inputBox.setHint("回复" +
                                allItems.get(position).getUser().getName());
                        parentCommentID = allItems.get(position).getId();
                    } else if (which == 1) {
                        Common.copy(mContext, allItems.get(position).getComment());
                    } else if (which == 2) {
                        Intent userIntent = new Intent(mContext, UserActivity.class);
                        userIntent.putExtra(Params.USER_ID, allItems.get(position)
                                .getUser().getId());
                        startActivity(userIntent);
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else if (viewType == 1) {
                Intent userIntent = new Intent(mContext, UserActivity.class);
                userIntent.putExtra(Params.USER_ID, allItems.get(position).getUser().getId());
                startActivity(userIntent);
            } else if (viewType == 2) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setItems(OPTIONS, (dialog, which) -> {
                    if (which == 0) {
                        baseBind.inputBox.setHint(
                                "回复" + allItems.get(position).getParent_comment().getUser().getName()
                        );
                        parentCommentID =
                                allItems.get(position).getParent_comment().getId();
                    } else if (which == 1) {
                        Common.copy(mContext, allItems.get(position).getParent_comment().getComment());
                    } else if (which == 2) {
                        Intent userIntent = new Intent(mContext, UserActivity.class);
                        userIntent.putExtra(Params.USER_ID, allItems.get(position)
                                .getParent_comment().getUser().getId());
                        startActivity(userIntent);
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            } else if (viewType == 3) {
                Intent userIntent = new Intent(mContext, UserActivity.class);
                userIntent.putExtra(Params.USER_ID, allItems.get(position).getParent_comment().getUser().getId());
                startActivity(userIntent);
            }
        });
    }

    @Override
    public String getToolbarTitle() {
        return title + "的评论";
    }

    @Override
    public void initRecyclerView() {
        baseBind.recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
    }

    @Override
    public void beforeFirstLoad(List<CommentsBean> items) {
        for (CommentsBean allItem : items) {
            String comment = allItem.getComment();
            if (Emoji.hasEmoji(comment)) {
                String newComment = Emoji.transform(comment);
                allItem.setComment(newComment);
            }

            if (allItem.getParent_comment() != null) {
                String parentComment = allItem.getParent_comment().getComment();
                if (Emoji.hasEmoji(parentComment)) {
                    String newComment = Emoji.transform(parentComment);
                    allItem.getParent_comment().setComment(newComment);
                }
            }
        }
    }

    @Override
    public void beforeNextLoad(List<CommentsBean> items) {
        for (CommentsBean allItem : items) {
            String comment = allItem.getComment();
            if (Emoji.hasEmoji(comment)) {
                String newComment = Emoji.transform(comment);
                allItem.setComment(newComment);
            }

            if (allItem.getParent_comment() != null) {
                String parentComment = allItem.getParent_comment().getComment();
                if (Emoji.hasEmoji(parentComment)) {
                    String newComment = Emoji.transform(parentComment);
                    allItem.getParent_comment().setComment(newComment);
                }
            }
        }
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        baseBind.post.setOnClickListener(v -> {
            if (!sUserModel.getResponse().getUser().isIs_mail_authorized()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("发布评论需要先绑定邮箱");
                builder.setPositiveButton("立即绑定", (dialog, which) -> {
                    Intent intent = new Intent(mContext, TemplateActivity.class);
                    intent.putExtra(TemplateActivity.EXTRA_FRAGMENT, "绑定邮箱");
                    startActivity(intent);
                });
                builder.setNegativeButton("取消", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog
                        .getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(
                                getResources().getColor(R.color.colorPrimary)
                        );
                alertDialog
                        .getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(
                                getResources().getColor(R.color.colorPrimary)
                        );
                return;
            }

            if (baseBind.inputBox.getText().toString().length() == 0) {
                Common.showToast("请输入评论内容", baseBind.inputBox, 3);
                return;
            }

            NullCtrl<CommentHolder> nullCtrl = new NullCtrl<CommentHolder>() {
                @Override
                public void onSubscribe(Disposable d) {
                    Common.hideKeyboard(mActivity);
                    mHelper.resetState();
                    baseBind.inputBox.setHint("请输入评论内容");
                    baseBind.inputBox.setText("");
                    baseBind.progress.setVisibility(View.VISIBLE);
                }

                @Override
                public void success(CommentHolder commentHolder) {
                    if (allItems.size() == 0) {
                        mRecyclerView.setVisibility(View.VISIBLE);
                        noData.setVisibility(View.INVISIBLE);
                    }

                    if (Emoji.hasEmoji(commentHolder.getComment().getComment())) {
                        commentHolder.getComment().setComment(
                                Emoji.transform(commentHolder.getComment().getComment()));
                        allItems.add(0, commentHolder.getComment());
                    } else {
                        allItems.add(0, commentHolder.getComment());
                    }
                    mAdapter.notifyItemInserted(0);
                    baseBind.recyclerView.scrollToPosition(0);
                }

                @Override
                public void must(boolean isSuccess) {
                    baseBind.progress.setVisibility(View.GONE);
                }
            };
            if (parentCommentID != 0) {
                Retro.getAppApi().postComment(sUserModel.getResponse().getAccess_token(), illustID,
                        baseBind.inputBox.getText().toString(), parentCommentID)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(nullCtrl);
            } else {
                Retro.getAppApi().postComment(sUserModel.getResponse().getAccess_token(), illustID,
                        baseBind.inputBox.getText().toString())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(nullCtrl);
            }
        });
        baseBind.clear.setOnClickListener(v -> {
            if (baseBind.inputBox.getText().toString().length() != 0) {
                baseBind.inputBox.setText("");
                return;
            }
            if (parentCommentID != 0) {
                baseBind.inputBox.setHint("留下你的评论吧");
                parentCommentID = 0;
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.recy_list);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 6);
        recyclerView.setLayoutManager(layoutManager);
        EmojiAdapter adapter = new EmojiAdapter(Emoji.getEmojis(), getContext());
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position, int viewType) {
                final EmojiItem item = adapter.getItemAt(position);

                String name = item.getName();
                String show = baseBind.inputBox.getText().toString();
                if (selection < show.length()) {
                    String left = show.substring(0, selection);
                    String right = show.substring(selection);

                    baseBind.inputBox.setText(left + name + right);
                    baseBind.inputBox.setSelection(selection + name.length());
                } else {
                    String result = show + name;

                    baseBind.inputBox.setText(result);
                    baseBind.inputBox.setSelection(result.length());
                }
                Common.showLog(className + selection);

            }
        });
        recyclerView.setAdapter(adapter);
        baseBind.inputBox.setOnSelectionChange(new EditTextWithSelection.OnSelectionChange() {
            @Override
            public void onChange(int start, int end) {
                if (start != 0) {
                    selection = start;
                }
            }
        });
    }
}
