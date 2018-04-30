package com.woefe.shoppinglist.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.woefe.shoppinglist.R;
import com.woefe.shoppinglist.shoppinglist.ListItem;
import com.woefe.shoppinglist.shoppinglist.ShoppingList;

/**
 * @author Wolfgang Popp
 */
public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.ViewHolder> {
    private final int colorChecked;
    private final int colorDefault;
    private final int colorRemove;
    private final int colorBackground;
    private ShoppingList shoppingList;
    private ItemTouchHelper touchHelper;
    private ItemLongClickListener longClickListener;

    private final ShoppingList.ShoppingListListener listener = new ShoppingList.ShoppingListListener() {
        @Override
        public void onShoppingListUpdate(ShoppingList list, ShoppingList.Event e) {
            switch (e.getState()) {
                case ShoppingList.Event.ITEM_CHANGED:
                    notifyItemChanged(e.getIndex());
                    break;
                case ShoppingList.Event.ITEM_INSERTED:
                    notifyItemInserted(e.getIndex());
                    break;
                case ShoppingList.Event.ITEM_MOVED:
                    notifyItemMoved(e.getOldIndex(), e.getNewIndex());
                    break;
                case ShoppingList.Event.ITEM_REMOVED:
                    notifyItemRemoved(e.getIndex());
                    break;
                default:
                    notifyDataSetChanged();
            }
        }
    };

    public RecyclerListAdapter(Context ctx) {
        colorChecked = ContextCompat.getColor(ctx, R.color.textColorChecked);
        colorDefault = ContextCompat.getColor(ctx, R.color.textColorDefault);
        colorRemove = ContextCompat.getColor(ctx, R.color.colorCritical);
        colorBackground = ContextCompat.getColor(ctx, R.color.colorListItemBackground);
        touchHelper = new ItemTouchHelper(new RecyclerListCallback());
    }

    public void connectShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
        shoppingList.addListener(listener);
        notifyDataSetChanged();
    }

    public void disconnectShoppingList() {
        shoppingList.removeListener(listener);
        shoppingList = null;
    }

    public void move(int fromPos, int toPos) {
        shoppingList.move(fromPos, toPos);
    }

    public void remove(int pos) {
        shoppingList.remove(pos);
    }

    public void registerRecyclerView(RecyclerView view) {
        touchHelper.attachToRecyclerView(view);
    }

    public void setOnItemLongClickListener(ItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        ListItem listItem = shoppingList.get(position);
        holder.description.setText(listItem.getDescription());
        holder.quantity.setText(listItem.getQuantity());

        if (listItem.isChecked()) {
            holder.description.setTextColor(colorChecked);
            holder.quantity.setTextColor(colorChecked);
        } else {
            holder.description.setTextColor(colorDefault);
            holder.quantity.setTextColor(colorDefault);
        }

        holder.itemView.setBackgroundColor(colorBackground);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shoppingList.toggleChecked(holder.getAdapterPosition());
            }
        });


        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return longClickListener != null
                        && longClickListener.onLongClick(holder.getAdapterPosition());
            }
        });

        holder.dragHandler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchHelper.startDrag(holder);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        if (shoppingList != null) {
            return shoppingList.size();
        }
        return 0;
    }

    public interface ItemLongClickListener {
        boolean onLongClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView description;
        TextView quantity;
        ImageView dragHandler;
        View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            description = itemView.findViewById(R.id.text_description);
            quantity = itemView.findViewById(R.id.text_quantity);
            dragHandler = itemView.findViewById(R.id.drag_n_drop_handler);
        }
    }

    public class RecyclerListCallback extends ItemTouchHelper.Callback {

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if (viewHolder.getItemViewType() != target.getItemViewType()) {
                return false;
            }

            RecyclerListAdapter.this.move(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            RecyclerListAdapter.this.remove(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                // Fade out the view as it is swiped out of the parent's bounds
                final float alpha = 1.0f - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
                if (dX < 1) {
                    viewHolder.itemView.setBackgroundColor(colorBackground);
                } else {
                    viewHolder.itemView.setBackgroundColor(colorRemove);
                }
                viewHolder.itemView.setAlpha(alpha);
                viewHolder.itemView.setTranslationX(dX);
            } else {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }

    }
}
