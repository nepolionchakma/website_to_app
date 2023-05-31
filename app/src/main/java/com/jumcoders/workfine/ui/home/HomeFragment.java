package com.jumcoders.workfine.ui.home;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jumcoders.workfine.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private WebView earnWebView;
    private TextView noInternetTextView  ;
    private ProgressBar progressBar  ;
    private FragmentHomeBinding binding;
    private AlertDialog exitConfirmationDialog;
    private boolean isNetworkAvailable = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        earnWebView = binding.idWebViewHome;
        noInternetTextView = binding.noInternetTextView;
        progressBar = binding.idPBLoading;

        // Enable JavaScript in WebView
        WebSettings webSettings = earnWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);

        // Enable HTML5 support in WebView
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        earnWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        earnWebView.loadUrl("https://www.google.com/");

        // Create the exit confirmation dialog
        //    BackPress
        exitConfirmationDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Exit the app
                        getActivity().finish();
                    }
                })
                .setNegativeButton("No", null)
                .create();

        earnWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                progressBar.setVisibility(View.VISIBLE);
                if (url.startsWith("tel:") || url.startsWith("whatsapp:") || url.startsWith("mail:")){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }else{
                    view.loadUrl(url);
                }

                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Handle error loading page (no internet connection)
//                showNoInternetError();
                // Page is loaded successfully
                /*earnWebView.setVisibility(View.GONE);
                noInternetTextView.setVisibility(View.VISIBLE);*/
                noInternetTextView.setVisibility(View.VISIBLE);
                earnWebView.setVisibility(View.GONE);
                checkInternetConnection();
                earnWebView.reload();
            }
        });



        earnWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

                // Check if the WRITE_EXTERNAL_STORAGE permission is granted
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Get the file name from the URL
                    String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                    String cookies = CookieManager.getInstance().getCookie(url);

                    // Create the download request
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                            .addRequestHeader("cookie", cookies)
                            .addRequestHeader("User-Agent", userAgent)
                            .setTitle(fileName)
                            .setDescription("Downloading")
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

                    // Get the system's DownloadManager service
                    DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

                    // Enqueue the download request
                    if (downloadManager != null) {
                        downloadManager.enqueue(request);
                        Toast.makeText(getActivity(), "Download started", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);

                    }
                } else {
                    // Request the WRITE_EXTERNAL_STORAGE permission if not granted
                    requestStoragePermission();
                }
            }

            private void requestStoragePermission() {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // Explain why the permission is needed
                    Toast.makeText(getActivity(), "Storage permission is required to download files", Toast.LENGTH_LONG).show();
                }

                // Request the permission
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                progressBar.setVisibility(View.GONE);

            }


        });

        /*earnWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_DOWN){
                    switch (keyCode){
                        case KeyEvent.KEYCODE_BACK:
                            if (earnWebView.canGoBack()){
                                earnWebView.goBack();
                            }else {
                                exitConfirmationDialog = new AlertDialog.Builder(getActivity())
                                        .setTitle("Exit")
                                        .setMessage("Are you sure you want to exit?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                // Exit the app
                                                getActivity().finish();
                                            }
                                        })
                                        .setNegativeButton("No", null)
                                        .create();
                            }
                    }
                }
                return false;
            }
        });*/
       /* // Check for internet connectivity
        if (!isNetworkAvailable()) {
            // Show a message or take appropriate action when there is no internet
            noInternetTextView.setVisibility(View.VISIBLE);
            earnWebView.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
        }*/


        return root;
    }

    //    BackPress
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setWebViewBackKeyListener();
    }
    private void setWebViewBackKeyListener() {
        earnWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (earnWebView.canGoBack()) {
                        earnWebView.goBack();
                        return true;
                    } else {
                        showExitDialog();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void showExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void checkInternetConnection() {
        isNetworkAvailable = isNetworkAvailable();
        if (isNetworkAvailable) {
            noInternetTextView.setVisibility(View.GONE);
            earnWebView.setVisibility(View.VISIBLE);
            earnWebView.reload();
            earnWebView.setVisibility(View.VISIBLE);
            noInternetTextView.setVisibility(View.GONE);
        }
        else {
            earnWebView.reload();
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check the internet connection when the fragment is resumed
        checkInternetConnection();
    }
    /*@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }*/
}