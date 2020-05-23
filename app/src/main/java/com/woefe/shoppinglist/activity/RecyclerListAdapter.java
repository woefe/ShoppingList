package com.woefe.shoppinglist.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.afollestad.sectionedrecyclerview.SectionedViewHolder;
import com.woefe.shoppinglist.R;
import com.woefe.shoppinglist.shoppinglist.ListItem;
import com.woefe.shoppinglist.shoppinglist.ShoppingList;

import java.util.ArrayList;

/**
 * @author Wolfgang Popp
 */
public class RecyclerListAdapter extends SectionedRecyclerViewAdapter<SectionedViewHolder> {
    private final int colorChecked;
    private final int colorDefault;
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
                    Log.d("LIST", "");
                    Log.d("LIST", "------------");
                    for (ListItem item : shoppingList.getCategories().get("Allgemein")) {
                        Log.d("LIST", item.getDescription());
                    }
                    Log.d("LIST", "------------");
                    Log.d("LIST", "");
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
        colorBackground = ContextCompat.getColor(ctx, R.color.colorListItemBackground);
        touchHelper = new ItemTouchHelper(new RecyclerListCallback(ctx));
    }

    public void connectShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
        shoppingList.addListener(listener);
        notifyDataSetChanged();
    }

    public void disconnectShoppingList() {
        if (shoppingList != null) {
            shoppingList.removeListener(listener);
            shoppingList = null;
        }
    }

    public void move(String category,
                     int fromPositionInCategory,
                     int toPositionInCategory,
                     int fromAbsolutePosition,
                     int toAbsolutePosition) {
        shoppingList.moveInCategory(category,
                fromPositionInCategory,
                toPositionInCategory,
                fromAbsolutePosition,
                toAbsolutePosition
        );
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
    public SectionedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_category, parent, false);
            return new CategoryViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public int getSectionCount() {
        return shoppingList.getCategories().size();
    }

    @Override
    public int getItemCount(int section) {
        return shoppingList.getCategories().valueAt(section).size();
    }

    @Override
    public void onBindHeaderViewHolder(SectionedViewHolder sectionedViewHolder, int section, boolean expanded) {
        CategoryViewHolder categoryViewHolder = (CategoryViewHolder) sectionedViewHolder;
        categoryViewHolder.category.setText(shoppingList.getCategories().keyAt(section));
    }

    @Override
    public void onBindFooterViewHolder(SectionedViewHolder sectionedViewHolder, int i) {
        // not needed
    }

    @Override
    public void onBindViewHolder(SectionedViewHolder sectionedViewHolder,
                                 int section,
                                 int relativePosition,
                                 final int absolutePosition) {
        final ItemViewHolder itemViewHolder = (ItemViewHolder) sectionedViewHolder;
        ArrayList<ListItem> list = shoppingList.getCategories().valueAt(section);
        final ListItem listItem = list.get(relativePosition);

        itemViewHolder.description.setText(listItem.getDescription());
        itemViewHolder.quantity.setText(listItem.getQuantity());
        itemViewHolder.listItem = listItem;

        if (listItem.isChecked()) {
            itemViewHolder.description.setTextColor(colorChecked);
            itemViewHolder.quantity.setTextColor(colorChecked);
        } else {
            itemViewHolder.description.setTextColor(colorDefault);
            itemViewHolder.quantity.setTextColor(colorDefault);
        }

        itemViewHolder.itemView.setBackgroundColor(colorBackground);

        itemViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shoppingList.toggleChecked(listItem, absolutePosition);
            }
        });

        itemViewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return longClickListener != null
                        && longClickListener.onLongClick(listItem);
            }
        });

        itemViewHolder.dragHandler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchHelper.startDrag(itemViewHolder);
                    return true;
                }
                return false;
            }
        });
    }

    public interface ItemLongClickListener {
        boolean onLongClick(ListItem item);
    }

    static class CategoryViewHolder extends SectionedViewHolder {
        TextView category;

        CategoryViewHolder(View itemView) {
            super(itemView);

            category = itemView.findViewById(R.id.category);
        }
    }

    static class ItemViewHolder extends SectionedViewHolder {
        TextView description;
        TextView quantity;
        ImageView dragHandler;
        View view;
        ListItem listItem;

        public ItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            description = itemView.findViewById(R.id.text_description);
            quantity = itemView.findViewById(R.id.text_quantity);
            dragHandler = itemView.findViewById(R.id.drag_n_drop_handler);
        }

        public int getPositionInCategory() {
            return getRelativePosition().relativePos();
        }
    }

    public class RecyclerListCallback extends ItemTouchHelper.Callback {
        private ColorDrawable background;
        private Drawable deleteIcon;
        private int backgroundColor;

        public RecyclerListCallback(Context ctx) {
            this.background = new ColorDrawable();
            this.deleteIcon = ContextCompat.getDrawable(ctx, R.drawable.ic_delete_forever_white_24);
            this.backgroundColor = ContextCompat.getColor(ctx, R.color.colorCritical);
        }

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
            final int swipeFlags = ItemTouchHelper.START;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if (viewHolder.getItemViewType() != target.getItemViewType()) {
                return false;
            }

//            if (!(viewHolder instanceof ItemViewHolder) || !(target instanceof ItemViewHolder)) {
//                return false;
//            }

            ItemViewHolder sourceViewHolder = (ItemViewHolder) viewHolder;
            ItemViewHolder targetViewHolder = (ItemViewHolder) target;

            RecyclerListAdapter.this.move(sourceViewHolder.listItem.getCategory(),
                    sourceViewHolder.getPositionInCategory(),
                    targetViewHolder.getPositionInCategory(),
                    sourceViewHolder.getAdapterPosition(),
                    targetViewHolder.getAdapterPosition());


//           RecyclerListAdapter.this.move(shoppingList.indexOf(sourceViewHolder.listItem),
//                   shoppingList.indexOf(targetViewHolder.listItem));

            // RecyclerListAdapter.this.move(sourceViewHolder.getAdapterPosition(), targetViewHolder.getAdapterPosition());
            
            
            return true;
        }

//        @Override
//        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
//            super.clearView(recyclerView, viewHolder);
//
//            if(dragFrom != -1 && dragTo != -1 && dragFrom != dragTo) {
//                reallyMoved(dragFrom, dragTo);
//            }
//
//            dragFrom = dragTo = -1;
//        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            RecyclerListAdapter.this.remove(viewHolder.getAdapterPosition());
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                return;
            }

            View itemView = viewHolder.itemView;

            int backgroundLeft = itemView.getRight() + (int) dX;
            background.setBounds(backgroundLeft, itemView.getTop(), itemView.getRight(), itemView.getBottom());
            background.setColor(backgroundColor);
            background.draw(c);

            int itemHeight = itemView.getBottom() - itemView.getTop();
            int intrinsicHeight = deleteIcon.getIntrinsicHeight();
            int iconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int iconMargin = (itemHeight - intrinsicHeight) / 2;
            int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            int iconBottom = iconTop + intrinsicHeight;
            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            deleteIcon.draw(c);

            // Fade out the view as it is swiped out of the parent's bounds
            final float alpha = 1.0f - Math.abs(dX) / (float) itemView.getWidth();
            itemView.setAlpha(alpha);
            itemView.setTranslationX(dX);
        }
    }
}
