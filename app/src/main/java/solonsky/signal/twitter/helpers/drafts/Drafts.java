package solonsky.signal.twitter.helpers.drafts;

import android.content.Context;

import com.anupcowkur.reservoir.Reservoir;
import com.anupcowkur.reservoir.ReservoirGetCallback;
import com.google.common.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kmoaz on 30.08.2017.
 */

public class Drafts implements IDrafts {
    private long id;
    private boolean isLoadedAll = false;
    private DraftModel localDraft;
    private Context mContext;

    public Drafts(Context mContext, long id) {
        this.mContext = mContext;
        this.id = id;
    }

    @Override
    public void Save(DraftModel draft) {
        saveDraft(draft);
    }

    @Override
    public void loadAll(ReservoirGetCallback<List<DraftModel>> callback) {
        this.isLoadedAll = true;
        Type resultType = new TypeToken<List<DraftModel>>() {}.getType();
        Reservoir.getAsync(String.valueOf(id), resultType, callback);
    }

    @Override
    public void deleteDraft(final String text) {
        final ArrayList<DraftModel> draftsArray = new ArrayList<>();
        loadAll(new ReservoirGetCallback<List<DraftModel>>() {
            @Override public void onFailure(Exception e) {}
            @Override public void onSuccess(List<DraftModel> drafts) {
                draftsArray.addAll(drafts);
                for (DraftModel draft : draftsArray) {
                    if (draft.message.toLowerCase().equals(text.toLowerCase())) {
                        draftsArray.remove(draft);
                        break;
                    }
                }

                try {
                    Reservoir.put(String.valueOf(id), draftsArray);
                } catch (IOException e) {}
            }
        });
    }

    private void saveDraft (final DraftModel draft) {
        final ArrayList<DraftModel> draftsArray = new ArrayList<>();
        loadAll(new ReservoirGetCallback<List<DraftModel>>() {
            @Override
            public void onSuccess(List<DraftModel> drafts) {
                draftsArray.addAll(drafts);
                draftsArray.add(0, draft);
                try {
                    Reservoir.put(String.valueOf(id), draftsArray);
                } catch (IOException e) {}
            }

            @Override
            public void onFailure(Exception e) {
                draftsArray.add(0, draft);
                try {
                    Reservoir.put(String.valueOf(id), draftsArray);
                } catch (IOException e1) {}
            }
        });
    }
}