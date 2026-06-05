package com.example.wordclash.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wordclash.R;
import com.example.wordclash.models.User;

import java.util.ArrayList;
import java.util.List;

// הגדרת מחלקה ציבורית בשם UserAdapter, שיורשת את כל התכונות של אדפטר של אנדרואיד.
// הadapter יעבוד עם ViewHolder, שנמצא כאן בתוך הקובץ (בסוף הקובץ)
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    // private זה משתנה שנגיש אך ורק בתוך הקובץ הזה.
    // final אומר שברגע שנוצרה רשימה, לא ניתן להחליף אותה עם רשימות אחרות, אבל כן ניתן להכניס ולהויא ממנה.
    private final List<User> userList;
    private final OnUserClickListener onUserClickListener; // משתנה ששומר את הlistener, כדי שיטפל בלחיצות על משתמשים ברשימה.


    // הבנאי של המחלקה. כאשר יוצרים את האדפטר במסך כלשהוא, הוא נקרא.
    // Nullable אומר שמותר להעביר רק null (מותר ליצור adapter בלי להגדיר לו מאזין ללחיצות)
    // final מבטיח שהפרמטר לא ישתנה בתוך הפונקציה
    public UserAdapter(@Nullable final OnUserClickListener onUserClickListener) {
        //מאתחל רשימה ריקה חדשה מסוג ArrayList ברגע שהאדפטר נוצר, כדי שלא נקבל שגיאת קריסה (NullPointerException)
        userList = new ArrayList<>();
        //לוקח את המאזין שקיבלנו בבנאי ושומר אותו בתוך המשתנה של המחלקה (this) כדי שנוכל להשתמש בו בהמשך.
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull // @NonNull: הבטחה למערכת שהפונקציה הזו לעולם לא תחזיר ערך ריק (null).
    @Override // @Override: אומר שאנחנו דורסים ומממשים פונקציה קבועה של מחלקת האם של אנדרואיד.
    // הגדרה של הפונקציה. היא מקבלת את ה-parent (ה-RecyclerView עצמו שבו השורות יישבו) ואת סוג התצוגה (viewType).
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // מביא את כלי ה-"ניפוח" של אנדרואיד, שמשתמש בקונטקסט של הרשימה.
        // ה-ViewHolder הוא פשוט "מחסן קטן" שמחזיק את הרכיבים האלה בזיכרון, כדי שלא נצטרך לעשות findViewById בכל פעם ששורה זזה
        // ה-holder הוא פשוט הכינוי של השורה הספציפית שאותה המערכת ממחזרת ומציגה כרגע על המסך.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        if (user == null) return;

        // Set full username
        holder.tvUserName.setText(user.getUserName());
        holder.tvEmail.setText(user.getEmail());

        // Set initial in circle
        String initial = "";
        if (user.getUserName() != null && !user.getUserName().isEmpty()) {
            initial = String.valueOf(user.getUserName().charAt(0)).toUpperCase();
        }
        holder.tvUserInitial.setText(initial);

        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onClick(user);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onLongClick(user);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<User> users) {
        userList.clear();
        userList.addAll(users);
    }

    public void addUser(User user) {
        userList.add(user);
        notifyItemInserted(userList.size() - 1);
    }

    public void updateUser(User user) {
        int index = userList.indexOf(user);
        if (index == -1) return;
        userList.set(index, user);
        notifyItemChanged(index);
    }

    public void removeUser(User user) {
        int index = userList.indexOf(user);
        if (index == -1) return;
        userList.remove(index);
        notifyItemRemoved(index);
    }

    public interface OnUserClickListener {
        void onClick(User user);

        void onLongClick(User user);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvUserName;
        final TextView tvEmail;
        final TextView tvUserInitial;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_item_user_name);
            tvEmail = itemView.findViewById(R.id.tv_item_user_email);
            tvUserInitial = itemView.findViewById(R.id.userInitial);
        }
    }
}