package com.unipi.chrisavg.eventity.ui.bot;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.unipi.chrisavg.eventity.R;
import com.unipi.chrisavg.eventity.databinding.FragmentBotBinding;
import com.unipi.chrisavg.eventity.databinding.FragmentTicketsBinding;

import java.util.Locale;


public class BotFragment extends Fragment {

    private FragmentBotBinding binding;

    private WebView webView;

    private View loadingLayout; // Reference to the loading layout


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBotBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        loadingLayout = root.findViewById(R.id.loading_layout);

        // Set the desired locale to "en-US"
        Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);

        // Create a configuration with the new locale
        Configuration config = new Configuration();
        config.locale = locale;

        webView = root.findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Hide the progress bar when the page finishes loading
                loadingLayout.setVisibility(View.GONE);
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Load the Bot Web Chat URL
        webView.loadUrl("https://webchat.botframework.com/embed/EventityAssistant?s=StQh1uebDpQ.s-q8zDWn8ifwEPaD0c6lpsMpg1FOlMSHVU2Bzwefy2E");


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}