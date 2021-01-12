package com.personal.project.explora;

import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.personal.project.explora.MainActivity;
import com.personal.project.explora.R;

public class EpisodePopupItemClickListener implements PopupMenu.OnMenuItemClickListener {

    private int position;
    public EpisodePopupItemClickListener(int position) {
        this.position = position;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_completed:
                if (item.getTitle().toString().contains("+"))
                    item.setTitle(item.getTitle().toString().substring(1));
                else
                    item.setTitle(item.getTitle() + "+");
                break;
            case R.id.item_download:
                if (item.getTitle().toString().contains("*"))
                    item.setTitle(item.getTitle().toString().substring(1));
                else
                    item.setTitle(item.getTitle() + "*");
                break;
            case R.id.item_queue:
                if (item.getTitle().toString().contains("-"))
                    item.setTitle(item.getTitle().toString().substring(1));
                else
                    item.setTitle(item.getTitle() + "-");
        }
        return false;
    }
}
