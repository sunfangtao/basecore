package com.wxt.library.tree.pagetree;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.wxt.library.tree.listener.PageTreeUpdateListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

/**
 * Created by Administrator on 2018/3/5.
 */

public class PageTreeHelper<T extends PageTreeNode> {

    private List<T> oriNodeList = null;
    private Map<String, T> groupNode = null;
    private RecyclerView recyclerView;
    private boolean isNeedUpdate = false;
    private Handler handler;
    private ExecutorService service;

    public PageTreeHelper(RecyclerView recyclerView, final PageTreeUpdateListener<T> listener) {
        this.recyclerView = recyclerView;
        oriNodeList = new ArrayList<>();
        groupNode = new HashMap<>();
        service = Executors.newFixedThreadPool(4);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                List<T> list = (List<T>) msg.obj;
                isNeedUpdate = false;
                if (listener != null) {
                    listener.onUpdateFinish(list);
                }
            }
        };

        this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE && isNeedUpdate) {
                    synchronized (PageTreeHelper.class) {
                        if (isNeedUpdate) {
                            excuseUpdate();
                        }
                    }
                }
            }
        });
    }

    private void excuseUpdate() {
        List<T> list = getShowNodeList();
        Message message = new Message();
        message.obj = list;
        handler.sendMessage(message);
    }

    private void readyUpdate() {
        if (recyclerView.getScrollState() == SCROLL_STATE_IDLE) {
            excuseUpdate();
        } else {
            isNeedUpdate = true;
        }
    }

    public boolean isLeaf(T node) {
        if (node == null) {
            throw new IllegalArgumentException("node为空!");
        }
        return groupNode.get(node.getpId()) == null;
    }

    public boolean isExpand(T node) {
        if (node == null) {
            throw new IllegalArgumentException("node为空!");
        }
        return node.isExpand();
    }

    public void setExpand(T node, boolean isExpand) {
        synchronized (PageTreeHelper.class) {
            if (node == null) {
                throw new IllegalArgumentException("node为空!");
            }
            node.setExpand(isExpand);
            if (!isExpand) {
                deepExpand(node);
            }

            service.execute(new Runnable() {
                @Override
                public void run() {
                    readyUpdate();
                }
            });
        }
    }

    private void deepExpand(T node) {
        if (node == null) {
            throw new IllegalArgumentException("node为空!");
        }
        node.setExpand(false);
        for (T t : groupNode.values()) {
            if (t.getpId().equals(node.getId())) {
                deepExpand(t);
            }
        }
    }

    public int getNodeLevel(T node) {
        if (node == null) {
            throw new IllegalArgumentException("node为空!");
        }
        T parentNode = groupNode.get(node.getpId());
        if (parentNode != null) {
            return 1 + getNodeLevel(parentNode);
        } else {
            return 0;
        }
    }

    private List<T> getShowNodeList() {
        List<T> showNodeList = new ArrayList<>();
        int length = oriNodeList.size();
        for (int i = 0; i < length; i++) {
            T tempNode = oriNodeList.get(i);
            if (getNodeLevel(tempNode) == 0 || tempNode.isExpand() || (groupNode.get(tempNode.getpId()) != null && groupNode.get(tempNode.getpId()).isExpand())) {
                // 父级展开
                showNodeList.add(tempNode);
            }
        }
        return showNodeList;
    }

    /**
     * 添加新组数据，加锁，防止多个组同时添加
     *
     * @param nodeList
     */
    public void addBranchData(final List<T> nodeList) {
        service.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (PageTreeHelper.class) {
                    if (nodeList != null && nodeList.size() > 0) {
                        String parentId = nodeList.get(0).getpId();
                        String id = nodeList.get(0).getId();
                        if (TextUtils.isEmpty(id)) {
                            throw new IllegalArgumentException("id为空！");
                        }
                        if (TextUtils.isEmpty(parentId)) {
                            throw new IllegalArgumentException("pId为空！");
                        }

                        int length = nodeList.size();
                        for (int i = 0; i < length; i++) {
                            T node = nodeList.get(i);
                            if (!parentId.equals(node.getpId())) {
                                throw new IllegalArgumentException("数据不一致!");
                            }
                            groupNode.put(node.getId(), node);
                        }

                        length = oriNodeList.size();

                        if (length == 0) {
                            oriNodeList.clear();
                            oriNodeList.addAll(nodeList);
                        } else {
                            // 插入组
                            for (int i = 0; i < length; i++) {
                                String tempId = oriNodeList.get(i).getId();
                                if (tempId.equals(parentId)) {
                                    // 找到父亲组，插入子组信息
                                    oriNodeList.addAll(i + 1, nodeList);
                                    break;
                                }
                            }
                        }
                        readyUpdate();
                    }
                }
            }
        });
    }

    /**
     * 查找节点的直接孩子节点
     *
     * @param pId
     * @param length
     * @return
     */
    private int findDirectChildIndex(String pId, int length, int startIndex) {
        PageTreeNode node = groupNode.get(pId);
        if (node == null) {
            // 父节点就是根节点
            for (int i = startIndex + 1; i < length; i++) {
                if (groupNode.get(oriNodeList.get(i).getpId()) == null) {
                    return i;
                }
            }
            return length;
        } else {
            // 找到了爷爷
            for (int i = startIndex + 1; i < length; i++) {
                PageTreeNode tempNode = oriNodeList.get(i);
                if (tempNode.getpId().equals(node.getId())) {
                    return i;
                }
            }
            return findDirectChildIndex(node.getpId(), length, startIndex);
        }
    }

    /**
     * 添加新设备数据，加锁，防止同时添加
     *
     * @param nodeList
     */
    public void addLeafData(final List<T> nodeList) {
        service.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (PageTreeHelper.class) {
                    if (oriNodeList.size() == 0) {
                        throw new IllegalArgumentException("请先插入根节点！");
                    }

                    if (nodeList != null && nodeList.size() > 0) {
                        String parentId = nodeList.get(0).getpId();
                        String id = nodeList.get(0).getId();
                        if (TextUtils.isEmpty(id)) {
                            throw new IllegalArgumentException("id为空！");
                        }
                        if (TextUtils.isEmpty(parentId)) {
                            throw new IllegalArgumentException("pId为空！");
                        }

                        int length = nodeList.size();
                        for (int i = 1; i < length; i++) {
                            if (!parentId.equals(nodeList.get(i).getpId())) {
                                throw new IllegalArgumentException("数据不一致!");
                            }
                        }

                        length = oriNodeList.size();
                        int startIndex = 0;
                        for (int i = 0; i < length; i++) {
                            PageTreeNode node = oriNodeList.get(i);
                            if (node.getId().equals(parentId)) {
                                startIndex = i;
                            }
                        }
                        int index = findDirectChildIndex(groupNode.get(parentId).getpId(), length, startIndex);
                        // 插入设备
                        oriNodeList.addAll(index, nodeList);
                        readyUpdate();
                    }

                }
            }
        });

    }

}
