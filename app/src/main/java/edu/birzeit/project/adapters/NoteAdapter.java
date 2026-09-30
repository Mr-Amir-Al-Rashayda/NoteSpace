package edu.birzeit.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import edu.birzeit.project.R;
import edu.birzeit.project.models.Note;
import edu.birzeit.project.utils.DateUtils;

public class NoteAdapter extends ListAdapter<Note, NoteAdapter.NoteViewHolder> {
    public interface NoteActionListener {
        void onOpen(Note note);
        void onEdit(Note note);
        void onDelete(Note note);
        void onShare(Note note, View anchor);
        void onFavoriteChanged(Note note, boolean favorite, ImageButton button);
    }

    private final NoteActionListener listener;

    public NoteAdapter(NoteActionListener listener) { super(DIFF_CALLBACK); this.listener = listener; }

    private static final DiffUtil.ItemCallback<Note> DIFF_CALLBACK = new DiffUtil.ItemCallback<Note>() {
        @Override public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getId() == newItem.getId();
        }
        @Override public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView date;
        private final Chip tag;
        private final ImageButton favorite;
        private final ImageButton more;

        NoteViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.noteTitle);
            date = view.findViewById(R.id.noteDate);
            tag = view.findViewById(R.id.noteTag);
            favorite = view.findViewById(R.id.favoriteButton);
            more = view.findViewById(R.id.moreButton);
        }

        void bind(Note note) {
            title.setText(note.getTitle());
            date.setText(DateUtils.format(note.getCreatedAt()));
            if (note.getTag() == null || note.getTag().trim().isEmpty()) tag.setVisibility(View.GONE);
            else { tag.setVisibility(View.VISIBLE); tag.setText(note.getTag()); }
            favorite.setImageResource(note.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
            favorite.setContentDescription(itemView.getContext().getString(note.isFavorite()
                    ? R.string.remove_from_favorites : R.string.add_to_favorites));
            itemView.setOnClickListener(view -> listener.onOpen(note));
            favorite.setOnClickListener(view -> listener.onFavoriteChanged(note, !note.isFavorite(), favorite));
            more.setOnClickListener(view -> showActions(note));
        }

        private void showActions(Note note) {
            PopupMenu popupMenu = new PopupMenu(itemView.getContext(), more);
            popupMenu.inflate(R.menu.note_actions_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) listener.onEdit(note);
                else if (item.getItemId() == R.id.action_delete) listener.onDelete(note);
                else if (item.getItemId() == R.id.action_send) listener.onShare(note, itemView);
                else return false;
                return true;
            });
            popupMenu.show();
        }
    }
}
