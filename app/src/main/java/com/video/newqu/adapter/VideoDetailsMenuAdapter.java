package com.video.newqu.adapter;

import android.graphics.Color;
import com.video.newqu.R;
import com.video.newqu.bean.VideoDetailsMenu;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/12/10
 * 视频详情界面菜单
 */

public class VideoDetailsMenuAdapter extends BaseQuickAdapter<VideoDetailsMenu,BaseViewHolder> {

    public VideoDetailsMenuAdapter(List<VideoDetailsMenu> data) {
        super(R.layout.re_video_details_menu_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, VideoDetailsMenu item) {
        helper.setText(R.id.tv_item_name,item.getItemName());
        helper.setVisible(R.id.view_line,helper.getPosition()==getData().size()-1?false:true);
        helper.setTextColor(R.id.tv_item_name, Color.parseColor(item.getTextColor()));
    }
}
