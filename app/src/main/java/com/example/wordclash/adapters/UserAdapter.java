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

    // final אומר שברגע שנוצרה רשימה, לא ניתן להחליף אותה עם רשימות אחרות, אבל כן ניתן להכניס ולהויא ממנה.
    // רשימה של אובייקטי User (מחלקת מודל) ומאזין OnUserClickListener לטיפול בלחיצות רגילות וארוכות במסך.
    private final List<User> userList;
    private final OnUserClickListener onUserClickListener; // משתנה ששומר את הlistener, כדי שיטפל בלחיצות על משתמשים ברשימה.


    //מאתחל ArrayList ריקה ושומר את המאזין, כאשר הנוטציה Nullable אומרת שמותר ליצור אדפטר גם בלי להגדיר מאזין.
    public UserAdapter(@Nullable final OnUserClickListener onUserClickListener) {
        userList = new ArrayList<>();
        this.onUserClickListener = onUserClickListener;
    }

    @NonNull
    @Override
    // מנפח את ה-XML של פריט משתמש (item_user) ומחזיר ViewHolder שמחזיק את הרכיבים שלו.
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // מביא את כלי ה-"ניפוח" של אנדרואיד, שמשתמש בקונטקסט של הרשימה.
        // ה-ViewHolder הוא פשוט "מחסן קטן" שמחזיק את הרכיבים האלה בזיכרון, כדי שלא נצטרך לעשות findViewById בכל פעם ששורה זזה
        // ה-holder הוא פשוט הכינוי של השורה הספציפית שאותה המערכת ממחזרת ומציגה כרגע על המסך.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    //מציג שם ואימייל, שולף אות ראשונה בעזרת charAt(0) לעיצוב הפרופיל, ומגדיר אירועי לחיצה קצרה וארוכה.
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
            // החזרת true בלחיצה ארוכה מסמנת למערכת שהאירוע טופל לחלוטין, ומונעת הפעלה בטעות של לחיצה רגילה (onClick) מיד לאחר מכן.
            return true;
        });
    }

    // מחזיר את כמות המשתמשים ברשימה המקומית.
    @Override
    public int getItemCount() {
        return userList.size();
    }

    //מעדכן את הרשימה המקומית בנתונים חדשים: ה-Activity שקוראת לפונקציה זו צריכה לקרוא לאחר מכן ל-notifyDataSetChanged כדי לרענן את המסך.
    public void setUserList(List<User> users) {
        userList.clear();
        userList.addAll(users);
    }


    //מנהלות את הרשימה בצורה דינמית ומעדכנות שורות ספציפיות בלבד (בעזרת notifyItemInserted/Changed/Removed) לשיפור ביצועים ואנימציה חלקה.
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

    // האדפטר עצמו הוא רק רכיב תצוגה גרפי,
    // הוא לא יודע מה לעשות כשלוחצים על משתמש (למשל לפתוח מסך פרופיל או לחסום אותו).
    //לכן הגדרתי כאן ממשק (Interface) עם שתי פעולות חובה (לחיצה רגילה ולחיצה ארוכה).
    // ה-Activity שמפעילה את האדפטר תממש את הממשק הזה ותקבע בעצמה מה קורה בזמן אמת כשלוחצים.
    public interface OnUserClickListener {
        void onClick(User user);

        void onLongClick(User user);
    }


    //שומר את רכיבי השורה בזיכרון ומקשר אותם ב-findViewById פעם אחת בלבד בזמן יצירת השורה.
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