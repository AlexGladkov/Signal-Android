package solonsky.signal.twitter.helpers.drafts;

import com.anupcowkur.reservoir.ReservoirGetCallback;

import java.util.List;

/**
 * Created by kmoaz on 30.08.2017.
 */

public interface IDrafts {
    void Save (DraftModel draft);
    void loadAll (ReservoirGetCallback<List<DraftModel>> callback);
    void deleteDraft (String text);
}
