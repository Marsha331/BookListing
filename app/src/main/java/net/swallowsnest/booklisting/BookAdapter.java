package net.swallowsnest.booklisting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by marshas on 10/24/16.
 */

public class BookAdapter extends ArrayAdapter<Book> {
    public BookAdapter(Context context, List<Book> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //set up the View of the book
        View bookListView = convertView;
        if (bookListView == null) {
            bookListView = LayoutInflater.from(getContext()).inflate(
                    R.layout.book_list_item, parent, false);
        }

        //find book position
        Book currentBook = getItem(position);

        TextView titleTextView = (TextView) bookListView.findViewById(R.id.title);
        titleTextView.setText(currentBook.getTitle());

        TextView authorTextView = (TextView) bookListView.findViewById(R.id.author);
        authorTextView.setText(currentBook.getAuthor());

        return bookListView;
    }
}
