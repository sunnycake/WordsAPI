package com.mynuex.wordsapi;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "DICTIONARY";

    Button searchButton;
    EditText enterWord;
    TextView wordDefinition;
    TextView wordPronunciation;
    ImageView wordImage;
    ProgressBar loading;

    WordService wordService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://owlbot.info/api/v3/dictionary/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        wordService = retrofit.create(WordService.class);

        searchButton = findViewById(R.id.search_button);
        enterWord = findViewById(R.id.enter_word);
        wordDefinition = findViewById(R.id.word_definition);
        wordPronunciation = findViewById(R.id.word_pronunciation);
        wordImage = findViewById(R.id.word_image);
        loading = findViewById(R.id.loading);


        setSearchEnabled(true);

        // Hide until an image/pronunciation is available
        wordImage.setVisibility(GONE);
        wordPronunciation.setVisibility(GONE);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String word = enterWord.getText().toString();
                if (word.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter a word", Toast.LENGTH_SHORT).show();
                    return;
                }

                hideKeyboard();
                setSearchEnabled(false);
                getDefinitionForWord(word);
            }
        });
    }

    private void getDefinitionForWord(final String word) {

        wordService.getDefinition(word, BuildConfig.OWLBOT_TOKEN).enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull Response<Word> response) {

                setSearchEnabled(true);

                Word wordResponse = response.body();
                Log.d(TAG, "Word Response: " + wordResponse);

                if (wordResponse != null && wordResponse.definitions.length >= 1) {
                    wordDefinition.setText(wordResponse.definitions[0].definition);

                    String imageURL = wordResponse.definitions[0].image_url;
                    String wordPronounced = wordResponse.pronunciation;

                    if (wordPronounced != null || wordPronounced.isEmpty()) {
                        wordPronunciation.setVisibility(GONE);
                    } else {
                        wordPronunciation.setVisibility(VISIBLE);
                        wordPronunciation.setText(wordPronounced);
                    }
                    if (imageURL == null || imageURL.isEmpty()) {
                        wordImage.setVisibility(GONE);   // Hide, if no image available
                    } else {
                        wordImage.setVisibility(View.VISIBLE);
                        Picasso.get().load(imageURL).fit().centerCrop().into(wordImage);
                    }
                } else {
                    Log.d(TAG, "Search for " + word + " did not return any definitions");
                    Toast.makeText(MainActivity.this, "No definitions found for " + word, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {

                setSearchEnabled(true);
                Log.e(TAG, "Error fetching definition", t);
                Toast.makeText(MainActivity.this, "Unable to fetch definition", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideKeyboard() {
        View mainView = findViewById(android.R.id.content);
        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(mainView.getWindowToken(), 0);
    }

    private void setSearchEnabled(boolean isEnabled) {
        loading.setVisibility( isEnabled ? GONE : VISIBLE);
        searchButton.setEnabled(isEnabled);
        enterWord.setEnabled(isEnabled);
    }

}