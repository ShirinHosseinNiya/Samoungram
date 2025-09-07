package org.project.models;

public enum PacketType {
    SIGN_UP,
    LOGIN,
    LOGOUT,

    // پیام‌ها
    SEND_MESSAGE,
    SEND_GROUP_MESSAGE,
    SEND_CHANNEL_MESSAGE,
    NEW_MESSAGE,             // پیام جدید
    MESSAGE_STATUS_UPDATE,   // تغییر وضعیت پیام (Sent / Delivered / Read)

    // گروه‌ها و کانال‌ها
    CREATE_GROUP,
    ADD_GROUP_MEMBER,
    CREATE_CHANNEL,
    ADD_CHANNEL_MEMBER,

    // پروفایل و تنظیمات
    VIEW_PROFILE,
    UPDATE_PROFILE,
    CHANGE_PASSWORD,

    // دریافت داده
    FETCH_CHATS,     // درخواست لیست همه‌ی چت‌ها
    CHATS_LIST,      // پاسخ سرور: لیست چت‌ها
    FETCH_CHAT_HISTORY, // درخواست تاریخچه‌ی یک چت خاص
    MESSAGES_LIST,   // پاسخ سرور: لیست پیام‌ها
    TYPING,          // وضعیت در حال تایپ

    // عمومی
    ERROR,
    SUCCESS
}
