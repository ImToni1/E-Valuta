package com.example.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.example.app.api.ApiService; // Provjerite je li import ispravan
import com.example.app.api.Article;
import com.example.app.api.NewsResponse;
import com.example.app.api.Singleton; // Provjerite je li import ispravan

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity implements NewsAdapter.OnItemInteractionListener {

    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddNews;

    private List<Article> allFetchedArticles = new ArrayList<>();
    private int currentlyDisplayedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
        setupSwipeToDelete();

        if (!loadNewsState()) {
            fetchNews();
        }

        fabAddNews.setOnClickListener(v -> addNextArticle());
    }

    private void initViews() {
        newsRecyclerView = findViewById(R.id.news_recyclerview);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        fabAddNews = findViewById(R.id.fab_add_news);
    }

    private void setupRecyclerView() {
        newsAdapter = new NewsAdapter(this);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsRecyclerView.setAdapter(newsAdapter);
    }

    @Override
    public void onItemLongClick(int position, Article article) {
        showEditDialog(position, article);
    }

    private void showEditDialog(final int position, final Article article) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_news_title);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_news, null);
        builder.setView(dialogView);

        final EditText editTextNews = dialogView.findViewById(R.id.edit_text_news);
        editTextNews.setText(article.getTitle());

        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            String newText = editTextNews.getText().toString().trim();
            if (!newText.isEmpty()) {
                article.setTitle(newText);
                allFetchedArticles.set(position, article);
                newsAdapter.updateArticleAt(position, article);
                saveNewsState();
                Toast.makeText(this, R.string.news_updated, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }


    private void saveNewsState() {
        SharedPreferences sharedPreferences = getSharedPreferences("NewsState", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonArticles = gson.toJson(allFetchedArticles);
        editor.putString("ALL_ARTICLES_JSON", jsonArticles);
        editor.putInt("DISPLAYED_COUNT", currentlyDisplayedCount);
        editor.apply();
    }

    private boolean loadNewsState() {
        SharedPreferences sharedPreferences = getSharedPreferences("NewsState", MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonArticles = sharedPreferences.getString("ALL_ARTICLES_JSON", null);
        if (jsonArticles != null) {
            Type type = new TypeToken<ArrayList<Article>>() {}.getType();
            allFetchedArticles = gson.fromJson(jsonArticles, type);
            currentlyDisplayedCount = sharedPreferences.getInt("DISPLAYED_COUNT", 0);

            if (allFetchedArticles != null && currentlyDisplayedCount > 0 && currentlyDisplayedCount <= allFetchedArticles.size()) {
                List<Article> previouslyShownArticles = new ArrayList<>(allFetchedArticles.subList(0, currentlyDisplayedCount));
                newsAdapter.setArticles(previouslyShownArticles);
                return true;
            }
        }
        return false;
    }

    private void fetchNews() {
        ApiService apiService = Singleton.getApi();
        apiService.getNews().enqueue(new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allFetchedArticles = response.body().getData();
                    if (allFetchedArticles != null && !allFetchedArticles.isEmpty()) {
                        displayInitialArticles();
                    }
                } else {
                    Log.e("HomeActivity", "Response not successful. Code: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                Log.e("HomeActivity", "API call failed.", t);
            }
        });
    }

    private void displayInitialArticles() {
        int initialCount = Math.min(allFetchedArticles.size(), 2);
        List<Article> initialArticles = new ArrayList<>(allFetchedArticles.subList(0, initialCount));
        newsAdapter.setArticles(initialArticles);
        currentlyDisplayedCount = initialArticles.size();
        saveNewsState();
    }

    private void addNextArticle() {
        if (allFetchedArticles != null && currentlyDisplayedCount < allFetchedArticles.size()) {
            Article nextArticle = allFetchedArticles.get(currentlyDisplayedCount);
            newsAdapter.addOneArticle(nextArticle);
            currentlyDisplayedCount++;
            saveNewsState();
        } else {
            Toast.makeText(this, R.string.no_more_news, Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                showDeleteConfirmationDialog(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(newsRecyclerView);
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirmation_title)
                .setMessage(R.string.delete_confirmation_message)
                .setPositiveButton(R.string.yes_delete, (dialog, which) -> {
                    newsAdapter.removeArticleAt(position);
                    allFetchedArticles.remove(position);
                    currentlyDisplayedCount--;
                    saveNewsState();
                })
                .setNegativeButton(R.string.no_cancel, (dialog, which) -> newsAdapter.notifyItemChanged(position))
                .setCancelable(false)
                .show();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            if (itemId == R.id.nav_wallet) {
                startActivity(new Intent(getApplicationContext(), Wallet.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_trade) {
                startActivity(new Intent(getApplicationContext(), Trade.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(getApplicationContext(), Settings.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}