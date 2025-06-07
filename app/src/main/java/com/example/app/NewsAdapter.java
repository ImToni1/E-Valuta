package com.example.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.example.app.api.Article; // ISPRAVLJENO OVDJE

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<Article> articles = new ArrayList<>();
    private OnItemInteractionListener listener;

    public interface OnItemInteractionListener {
        void onItemLongClick(int position, Article article);
    }

    public NewsAdapter(OnItemInteractionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        Article currentArticle = articles.get(position);
        holder.bind(currentArticle, listener);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setArticles(List<Article> newArticles) {
        this.articles = new ArrayList<>(newArticles);
        notifyDataSetChanged();
    }

    public void removeArticleAt(int position) {
        articles.remove(position);
        notifyItemRemoved(position);
    }

    public void addOneArticle(Article article) {
        articles.add(article);
        notifyItemInserted(articles.size() - 1);
    }

    public void updateArticleAt(int position, Article article) {
        articles.set(position, article);
        notifyItemChanged(position);
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.item_news_title);
        }

        public void bind(final Article article, final OnItemInteractionListener listener) {
            titleTextView.setText(article.getTitle());
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemLongClick(position, article);
                    }
                }
                return true;
            });
        }
    }
}