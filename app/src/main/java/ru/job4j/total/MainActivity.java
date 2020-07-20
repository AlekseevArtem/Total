package ru.job4j.total;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mViewsForFiles;
    private File mDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_of_files);
        mViewsForFiles = findViewById(R.id.list_of_files);
        mViewsForFiles.setLayoutManager(new LinearLayoutManager(this));
        mDirectory = new File(Objects.requireNonNull(getExternalFilesDir(null)).getPath());
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
        } else return true;
    }

    private void updateUI(File directory) {
        setTitle(directory.getName());
        File[] files = directory.listFiles();
        List<File> allFiles = new ArrayList<>(Arrays.asList(files));
        mViewsForFiles.setAdapter(new FilesAdapter(allFiles));
    }

    private class FilesHolder extends RecyclerView.ViewHolder {
        private Button mNameButton;
        private File mFile;

        public FilesHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_file, parent,false));
            mNameButton = itemView.findViewById(R.id.media_name);
        }

        public void bind(File file) {
            mFile = file;
            mNameButton.setText(mFile.getName());
            if(mFile.isDirectory()) {
                mNameButton.setOnClickListener(this::clickOnDirectory);
            } else if (getExtensionByStringHandling(mFile.getName()).isPresent() &&
                    getExtensionByStringHandling(mFile.getName()).get().equals("mp3")) {
                mNameButton.setOnClickListener(this::clickOnMP3);
            }
        }

        public Optional<String> getExtensionByStringHandling(String filename) {
            return Optional.ofNullable(filename)
                    .filter(f -> f.contains("."))
                    .map(f -> f.substring(filename.lastIndexOf(".") + 1));
        }

        private void clickOnDirectory(View view){
            rewriteDirectory(mFile);
        }

        private void clickOnMP3(View view){
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (intent.resolveActivity(getPackageManager()) != null) {
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", mFile);
                intent.setDataAndType(photoURI, "audio/mp3");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
        }
    }

    private class FilesAdapter extends RecyclerView.Adapter<FilesHolder> {
        private List<File> mFiles;

        public FilesAdapter(List<File> crimes) {
            this.mFiles = crimes;
        }

        @NonNull
        @Override
        public FilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FilesHolder(LayoutInflater.from(getApplicationContext()), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull FilesHolder holder, int position) {
            holder.bind(mFiles.get(position));
        }

        @Override
        public int getItemCount() {
            return mFiles.size();
        }
    }
}