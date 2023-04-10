/*
 *                       Copyright (C) of Avery
 *
 *                              _ooOoo_
 *                             o8888888o
 *                             88" . "88
 *                             (| -_- |)
 *                             O\  =  /O
 *                          ____/`- -'\____
 *                        .'  \\|     |//  `.
 *                       /  \\|||  :  |||//  \
 *                      /  _||||| -:- |||||-  \
 *                      |   | \\\  -  /// |   |
 *                      | \_|  ''\- -/''  |   |
 *                      \  .-\__  `-`  ___/-. /
 *                    ___`. .' /- -.- -\  `. . __
 *                 ."" '<  `.___\_<|>_/___.'  >'"".
 *                | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 *                \  \ `-.   \_ __\ /__ _/   .-` /  /
 *           ======`-.____`-.___\_____/___.-`____.-'======
 *                              `=- -='
 *           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 *              Buddha bless, there will never be bug!!!
 */

package tv.danmaku.ijk.media.subtitle;


import tv.danmaku.ijk.media.subtitle.model.Subtitle;
import tv.danmaku.ijk.media.subtitle.runtime.AppTaskExecutor;

/**
 * @author AveryZhong.
 */

public class UIRenderTask implements Runnable {

    private Subtitle mSubtitle;
    private ISubtitleEngine.OnSubtitleChangeListener mOnSubtitleChangeListener;

    public UIRenderTask(final ISubtitleEngine.OnSubtitleChangeListener l) {
        mOnSubtitleChangeListener = l;
    }

    @Override
    public void run() {
        if (mOnSubtitleChangeListener != null) {
            mOnSubtitleChangeListener.onSubtitleChanged(mSubtitle);
        }
    }

    public void execute(final Subtitle subtitle) {
        mSubtitle = subtitle;
        AppTaskExecutor.mainThread().execute(this);
    }
}
