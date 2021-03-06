package com.video.newqu.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.SMSEventMessage;
import com.video.newqu.databinding.FragmentRegisterBinding;
import com.video.newqu.ui.activity.LoginGroupActivity;
import com.video.newqu.ui.contract.RegisterContract;
import com.video.newqu.ui.presenter.RegisterPresenter;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * TinyHung@Outlook.com
 * 2017/11/28.
 * 用户注册和填写密码
 */

public class LoginRegisterFragment extends BaseFragment<FragmentRegisterBinding,RegisterPresenter> implements RegisterContract.View {

    private Animation mInputAnimation;
    private Handler mHandler;
    private LoginGroupActivity mLoginGroupActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mLoginGroupActivity = (LoginGroupActivity) context;
    }

    @Override
    protected void initViews() {
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    //注册
                    case R.id.btn_register:
                        cureateRegisterUser();
                        break;
                    //获取验证码
                    case R.id.tv_get_code:
                        cureateGetNumberCode();
                        break;
                }
            }
        };
        bindingView.btnRegister.setOnClickListener(onClickListener);
        bindingView.tvGetCode.setOnClickListener(onClickListener);
        mInputAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_register;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler=new Handler();
        mPresenter = new RegisterPresenter(getActivity());
        mPresenter.attachView(this);
    }


    /**
     * 准备获取验证码
     */
    private void cureateGetNumberCode() {
        String account = bindingView.inputAccount.getEditContent();
        if(TextUtils.isEmpty(account)){
            ToastUtils.showCenterToast("手机号码不能为空");
            bindingView.inputAccount.startAnimation(mInputAnimation);
            return;
        }
        if(!Utils.isPhoneNumber(account)){
            ToastUtils.showCenterToast("手机号码格式不正确");
            return;
        }
        getCode("86",account);
    }

    /**
     * 获取验证码
     * @param country 区号
     * @param account 手机号码
     */
    private void getCode(String country, String account) {
        if(null!=mLoginGroupActivity&&!mLoginGroupActivity.isFinishing()){
            showProgressDialog("获取验证码中，请稍后...",true);
            mLoginGroupActivity.getCode(country,account);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 监听短信
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SMSEventMessage event) {
        if(null!=event){
            closeProgressDialog();
            switch (event.getSmsCode()) {
                //发送验证码失败
                case 99:
                    initGetCodeBtn();
                    try {
                        if(!TextUtils.isEmpty(event.getMessage())){
                            try {
                                JSONObject jsonObject=new JSONObject(event.getMessage());
                                if(null!=jsonObject&&jsonObject.length()>0){
                                    if(null!=jsonObject.getString("detail")){
                                        ToastUtils.showCenterToast(jsonObject.getString("detail"));
                                    }else{
                                        ToastUtils.showCenterToast("发送验证码失败");
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }catch (Exception e){

                    }
                    break;

                //获取支持的国家列表成功
                case 103:
//                    HashMap<String, String> stringStringHashMap = (HashMap<String, String>) event.getMessage();
//                    //保存至本地
//                    SerMap serMap=new SerMap();
//                    serMap.setMap(stringStringHashMap);
//                    VideoApplication.mACache.put(Constant.CACHE_COUNTRY_NUMBER_LIST,serMap,Constant.CACHE_TIME);
                    break;
                //短信验证码验证成功
                case 100:
                    break;
                //获取验证码成功
                case 102:
                    showGetCodeDisplay();
                    ToastUtils.showCenterToast("已成功发送验证码");
                    break;
                //短信验证码已提交完成
                case 101:
                    //请求后台服务器验证

                    break;

            }
        }
    }

    /**
     * 改变获取验证码按钮状态
     */
    private void showGetCodeDisplay() {
        if(null==bindingView) return;
        totalTime=60;
        bindingView.tvGetCode.setClickable(false);
        bindingView.tvGetCode.setTextColor(CommonUtils.getColor(R.color.coment_color));
        bindingView.tvGetCode.setBackgroundResource(R.drawable.btn_find_password_bg_gray);
        if(null!=mHandler&&null!=taskRunnable) mHandler.postDelayed(taskRunnable,0);
    }


    /**
     * 还原获取验证码按钮状态
     */
    private void initGetCodeBtn() {
        if(null==bindingView) return;
        totalTime=0;
        if(null!=taskRunnable&&null!=mHandler){
            mHandler.removeCallbacks(taskRunnable);
            mHandler.removeMessages(0);
        }
        bindingView.tvGetCode.setText("重新获取");
        bindingView.tvGetCode.setClickable(true);
        bindingView.tvGetCode.setTextColor(CommonUtils.getColor(R.color.login_hint));
        bindingView.tvGetCode.setBackgroundResource(R.drawable.square_login_background_orgin);
    }

    /**
     * 定时任务，模拟倒计时广告
     */
    private int totalTime=60;

    Runnable taskRunnable=new Runnable() {
        @Override
        public void run() {
            if(null==bindingView) return;
            bindingView.tvGetCode.setText(totalTime+"S后重试");
            totalTime--;
            if(totalTime<0){
                //还原
                initGetCodeBtn();
                return;
            }
            if(null!=mHandler) mHandler.postDelayed(this,1000);
        }
    };




    /**
     * 准备注册用户
     */
    private void cureateRegisterUser() {

        String account = bindingView.inputAccount.getEditContent();
        String password = bindingView.inputPassword.getEditContent();
        String code = bindingView.etCode.getText().toString().trim();

        if(TextUtils.isEmpty(account)){
            bindingView.inputAccount.startAnimation(mInputAnimation);
            ToastUtils.showCenterToast("手机号码不能为空");
            return;
        }
        if(!Utils.isPhoneNumber(account)){
            bindingView.inputAccount.startAnimation(mInputAnimation);
            ToastUtils.showCenterToast("手机号码格式不正确");
            return;
        }

        if(TextUtils.isEmpty(password)){
            bindingView.inputPassword.startAnimation(mInputAnimation);
            ToastUtils.showCenterToast("请设置密码");
            return;
        }

        if(!Utils.isPassword(password)){
            bindingView.inputPassword.startAnimation(mInputAnimation);
            ToastUtils.showCenterToast("密码格式不正确");
            return;
        }

        if(TextUtils.isEmpty(code)){
            bindingView.etCode.startAnimation(mInputAnimation);
            ToastUtils.showCenterToast("验证码不能为空");
            return;
        }

        if(!Utils.isNumberCode(code)){
            bindingView.etCode.startAnimation(mInputAnimation);
            ToastUtils.showCenterToast("验证码格式不正确");
            return;
        }

        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
        }
        if(null!= mPresenter &&!mPresenter.isRegister()){
            showProgressDialog("提交注册中...",true);
            // TODO: 2017/6/20 用户注册
            mPresenter.register(VideoApplication.mUuid,account,password,code);
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        closeProgressDialog();
        if(null!=mInputAnimation){
            mInputAnimation.cancel();
            mInputAnimation=null;
        }
        initGetCodeBtn();
        if(null!=mHandler){
            mHandler.removeCallbacks(taskRunnable);
            mHandler=null;
        }
        taskRunnable=null;
        bindingView=null;
    }


    //==========================================注册结果回调=========================================

    @Override
    public void showErrorView() {
        closeProgressDialog();
    }

    @Override
    public void complete() {

    }

    @Override
    public void registerResultError(String data) {
        closeProgressDialog();
        ToastUtils.showCenterToast(data);
    }

    @Override
    public void registerResultFinlish(String data) {
        closeProgressDialog();
        ToastUtils.showCenterToast(data);
        if(null!=mLoginGroupActivity&&!mLoginGroupActivity.isFinishing()){
            mLoginGroupActivity.login(bindingView.inputAccount.getEditContent(),bindingView.inputPassword.getEditContent());
        }
    }

    @Override
    public void registerError() {
        closeProgressDialog();
    }

    @Override
    public void imageUploadError() {

    }

    @Override
    public void imageUploadFinlish(String data) {

    }

    @Override
    public void needIploadImageLogo() {

    }
}
