package ru.job4j.total;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private File mDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_of_files);
        mRecyclerView = findViewById(R.id.list_of_files);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDirectory = new File(getExternalFilesDir(null).getPath());
        updateUI(mDirectory);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String path = savedInstanceState.getString("pathToDirectory");
        mDirectory = new File(Objects.requireNonNull(path));
        updateUI(mDirectory);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("pathToDirectory", mDirectory.getPath());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            rewriteDirectory(mDirectory.getParentFile());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (Objects.requireNonNull(mDirectory.getParentFile()).canRead()){
                    rewriteDirectory(mDirectory.getParentFile());
                } else if (isStoragePermissionGranted())
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void rewriteDirectory(File file) {
        mDirectory = file;
        updateUI(mDirectory);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else return true;
    }

    private void updateUI(File directory) {
        setTitle(directory.getName());
        File[] files = directory.listFiles();
        List<File> allFiles = new ArrayList<>(Arrays.asList(files));
        mRecyclerView.setAdapter(new MediaFilesAdapter(allFiles));
    }

    private class MediaFilesHolder extends RecyclerView.ViewHolder {
        private Button mNameButton;
        private File mFile;

        public MediaFilesHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_file, parent,false));
            mNameButton = itemView.findViewById(R.id.media_name);
        }

        public void bind(File file) {
            mFile = file;
            mNameButton.setText(mFile.getName());
            if(mFile.isDirectory()) {
                mNameButton.setOnClickListener(this::clickOnNameButton);
            }
        }

        private void clickOnNameButton(View view){
            rewriteDirectory(mFile);
        }
    }

    private class MediaFilesAdapter extends RecyclerView.Adapter<MediaFilesHolder> {
        private List<File> mFiles;

        public MediaFilesAdapter(List<File> crimes) {
            this.mFiles = crimes;
        }

        @NonNull
        @Override
        public MediaFilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MediaFilesHolder(LayoutInflater.from(getApplicationContext()), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull MediaFilesHolder holder, int position) {
            holder.bind(mFiles.get(position));
        }

        @Override
        public int getItemCount() {
            return mFiles.size();
        }
    }
}