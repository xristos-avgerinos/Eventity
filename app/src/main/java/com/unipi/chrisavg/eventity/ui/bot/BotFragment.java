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
    private View loadingLayout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentBotBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        loadingLayout = root.findViewById(R.id.loading_layout);

        // Ορίζουμε την επιθυμητή τοπική γλώσσα σε "en-US" για να βγάζει σωστά αποτελέσματα το bot
        Locale locale = new Locale("en", "US");
        Locale.setDefault(locale);

        // Δημιουργούμε ενα configuration με τη νέα τοπική γλώσσα
        Configuration config = new Configuration();
        config.locale = locale;

        webView = root.findViewById(R.id.webView);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                // Απόκρυψη του progress bar όταν ολοκληρωθεί η φόρτωση της σελίδας
                loadingLayout.setVisibility(View.GONE);
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Φορτώνουμε το Bot Web Chat URL
        webView.loadUrl("https://webchat.botframework.com/embed/EventityAssistant?s=StQh1uebDpQ.s-q8zDWn8ifwEPaD0c6lpsMpg1FOlMSHVU2Bzwefy2E");


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}