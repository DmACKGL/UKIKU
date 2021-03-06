package knf.kuma.explorer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;

import knf.kuma.R;
import knf.kuma.pojos.ExplorerObject;
import xdroid.toaster.Toaster;

public class FragmentFilesRoot extends FragmentBase implements FragmentFiles.SelectedListener, FragmentChapters.ClearInterface, ExplorerCreator.EmptyListener {

    private FragmentFiles files;
    private FragmentChapters chapters;
    private boolean isFiles = true;
    private String name;
    private OnFileStateChange stateChange;

    public FragmentFilesRoot() {
        files = FragmentFiles.get(this);
        chapters = FragmentChapters.get(this);
    }

    public static FragmentFilesRoot get() {
        return new FragmentFilesRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explorer_files, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (!files.isAdded())
            transaction.add(R.id.root, files, FragmentFiles.TAG);
        if (!chapters.isAdded())
            transaction.add(R.id.root, chapters, FragmentChapters.TAG);
        transaction.commit();
        super.onViewCreated(view, savedInstanceState);
    }

    private void setFragment(boolean isFiles, @Nullable ExplorerObject object) {
        if (stateChange != null)
            stateChange.onChange(isFiles);
        this.isFiles = isFiles;
        this.name = object != null ? object.name : null;
        ExplorerCreator.IS_FILES = isFiles;
        ExplorerCreator.FILES_NAME = object;
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (isFiles) {
            transaction.hide(chapters);
            transaction.show(files);
        } else {
            chapters.setObject(object);
            transaction.hide(files);
            transaction.show(chapters);
        }
        transaction.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
        transaction.commit();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            this.isFiles = savedInstanceState.getBoolean("isFiles", true);
            this.name = savedInstanceState.getString("name");
        }
        setFragment(ExplorerCreator.IS_FILES, ExplorerCreator.FILES_NAME);
        if (!ExplorerCreator.IS_CREATED)
            ExplorerCreator.start(getContext(), this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isFiles", isFiles);
        outState.putString("name", name);
    }

    public void setStateChange(OnFileStateChange stateChange) {
        this.stateChange = stateChange;
    }

    public void onRemoveAll() {
        if (name != null && chapters != null && getActivity() != null)
            new MaterialDialog.Builder(getActivity())
                    .content("¿Eliminar todos los capitulos de " + name + "?")
                    .positiveText("Eliminar")
                    .negativeText("Cancelar")
                    .onPositive((dialog, which) -> {
                        if (chapters != null)
                            chapters.deleteAll();
                    })
                    .build().show();
        else Toaster.toast("Error al borrar episodios");
    }

    @Override
    public void onSelected(ExplorerObject object) {
        setFragment(false, object);
    }

    @Override
    public void onClear() {
        setFragment(true, null);
    }

    @Override
    public void onEmpty() {
        if (files != null)
            files.onEmpty();
    }

    @Override
    public boolean onBackPressed() {
        if (isFiles) {
            return false;
        } else {
            setFragment(true, null);
            return true;
        }
    }
}
