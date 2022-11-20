package solonsky.signal.twitter.viewmodels;

import android.view.View;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import solonsky.signal.twitter.BR;

/**
 * Created by neura on 24.05.17.
 */

public class SupportViewModel extends BaseObservable {
    private String tips_1;
    private String tips_2;
    private String tips_3;
    private boolean isTip1;
    private boolean isTip2;
    private boolean isTip3;
    private boolean isStar1;
    private boolean isStar2;
    private boolean isStar3;

    public interface SupportClickHandler {
        void onFirstTipClick(View v);
        void onSecondTipClick(View v);
        void onThirdTipClick(View v);
        void onRestoreClick(View v);
        void onReviewClick(View v);
        void onCoffeeClick(View v);
        void onBeerClick(View v);
        void onCakeClick(View v);
    }

    public SupportViewModel(String tips_1, String tips_2, String tips_3) {
        this.tips_1 = tips_1;
        this.tips_2 = tips_2;
        this.tips_3 = tips_3;
        this.isTip1 = false;
        this.isTip2 = false;
        this.isTip3 = false;
        this.isStar1 = false;
        this.isStar2 = false;
        this.isStar3 = false;
    }

    @Bindable
    public String getTips_1() {
        return tips_1;
    }

    public void setTips_1(String tips_1) {
        this.tips_1 = tips_1;
        notifyPropertyChanged(BR.tips_1);
    }

    @Bindable
    public String getTips_2() {
        return tips_2;
    }

    public void setTips_2(String tips_2) {
        this.tips_2 = tips_2;
        notifyPropertyChanged(BR.tips_2);
    }

    @Bindable
    public String getTips_3() {
        return tips_3;
    }

    public void setTips_3(String tips_3) {
        this.tips_3 = tips_3;
        notifyPropertyChanged(BR.tips_3);
    }

    @Bindable
    public boolean isTip1() {
        return isTip1;
    }

    public void setTip1(boolean tip1) {
        isTip1 = tip1;
        notifyPropertyChanged(BR.tip1);
    }

    @Bindable
    public boolean isTip2() {
        return isTip2;
    }

    public void setTip2(boolean tip2) {
        isTip2 = tip2;
        notifyPropertyChanged(BR.tip2);
    }

    @Bindable
    public boolean isTip3() {
        return isTip3;
    }

    public void setTip3(boolean tip3) {
        isTip3 = tip3;
        notifyPropertyChanged(BR.tip3);
    }

    @Bindable
    public boolean isStar1() {
        return isStar1;
    }

    public void setStar1(boolean star1) {
        isStar1 = star1;
        notifyPropertyChanged(BR.star1);
    }

    @Bindable
    public boolean isStar2() {
        return isStar2;
    }

    public void setStar2(boolean star2) {
        isStar2 = star2;
        notifyPropertyChanged(BR.star2);
    }

    @Bindable
    public boolean isStar3() {
        return isStar3;
    }

    public void setStar3(boolean star3) {
        isStar3 = star3;
        notifyPropertyChanged(BR.star3);
    }
}
