import org.project.server.db.DBConnection;
import org.project.server.db.UserDAO;

import java.sql.Connection;
import java.sql.SQLException;
import org.project.server.db.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Scanner;
import java.util.UUID;

public class MainTest {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            UserDAO userDAO = new UserDAO(conn);
            GroupDAO groupDAO = new GroupDAO(conn);
            ChannelDAO channelDAO = new ChannelDAO(conn);
            MessageDAO messageDAO = new MessageDAO(conn);

            Scanner sc = new Scanner(System.in);
            UUID currentUser = null;

            while (currentUser == null) {
                System.out.println("\n1) Sign up  2) Login  0) Exit");
                System.out.print("Choice: ");
                String choice = sc.nextLine().trim();
                if ("0".equals(choice)) return;
                if ("1".equals(choice)) {
                    System.out.print("Username: ");
                    String u = sc.nextLine().trim();
                    System.out.print("Password: ");
                    String p = sc.nextLine();
                    System.out.print("Profile name: ");
                    String pn = sc.nextLine().trim();
                    try {
                        UUID id = userDAO.register(u, p, pn);
                        System.out.println(id != null ? "✅ Sign up OK. UserID = " + id : "⚠️ Sign up failed.");
                    } catch (SQLException ex) {
                        System.out.println("❌ DB error on sign up: " + ex.getMessage());
                    }
                } else if ("2".equals(choice)) {
                    System.out.print("Username: ");
                    String u = sc.nextLine().trim();
                    System.out.print("Password: ");
                    String p = sc.nextLine();
                    try {
                        UUID id = userDAO.login(u, p);
                        if (id != null) {
                            currentUser = id;
                            System.out.println("✅ Login OK. UserID = " + id);
                        } else {
                            System.out.println("⚠️ Login failed.");
                        }
                    } catch (SQLException ex) {
                        System.out.println("❌ DB error on login: " + ex.getMessage());
                    }
                } else {
                    System.out.println("Invalid choice.");
                }
            }

            while (true) {
                System.out.println("\n== Menu ==");
                System.out.println("1) Create Group");
                System.out.println("2) Add Member to Group");
                System.out.println("3) Create Channel");
                System.out.println("4) Add Member to Channel");
                System.out.println("5) Send PV Message");
                System.out.println("0) Exit");
                System.out.print("Choice: ");
                String choice = sc.nextLine().trim();

                if ("0".equals(choice)) break;

                try {
                    if ("1".equals(choice)) {
                        System.out.print("Group name: ");
                        String gname = sc.nextLine().trim();
                        UUID gid = UUID.randomUUID();
                        groupDAO.createGroup(gid, gname, currentUser);
                        System.out.println("✅ Group created. GroupID = " + gid);
                    } else if ("2".equals(choice)) {
                        System.out.print("GroupID (UUID): ");
                        UUID gid = UUID.fromString(sc.nextLine().trim());
                        System.out.print("Member username: ");
                        String uname = sc.nextLine().trim();
                        UUID memberId = userDAO.findUserIdByUsername(uname);
                        if (memberId == null) {
                            System.out.println("⚠️ User not found.");
                        } else {
                            groupDAO.addMemberToGroup(gid, memberId);
                            System.out.println("✅ Member added to group.");
                        }
                    } else if ("3".equals(choice)) {
                        System.out.print("Channel name: ");
                        String cname = sc.nextLine().trim();
                        UUID cid = UUID.randomUUID();
                        channelDAO.createChannel(cid, cname, currentUser);
                        System.out.println("✅ Channel created. ChannelID = " + cid);
                    } else if ("4".equals(choice)) {
                        System.out.print("ChannelID (UUID): ");
                        UUID cid = UUID.fromString(sc.nextLine().trim());
                        System.out.print("Member username: ");
                        String uname = sc.nextLine().trim();
                        UUID memberId = userDAO.findUserIdByUsername(uname);
                        if (memberId == null) {
                            System.out.println("⚠️ User not found.");
                        } else {
                            channelDAO.addMemberToChannel(cid, memberId);
                            System.out.println("✅ Member added to channel.");
                        }
                    } else if ("5".equals(choice)) {
                        System.out.print("Receiver username: ");
                        String uname = sc.nextLine().trim();
                        UUID rid = userDAO.findUserIdByUsername(uname);
                        if (rid == null) {
                            System.out.println("⚠️ User not found.");
                        } else {
                            System.out.print("Message text: ");
                            String content = sc.nextLine();
                            UUID mid = UUID.randomUUID();
                            Timestamp ts = new Timestamp(System.currentTimeMillis());
                            messageDAO.saveMessage(mid, currentUser, rid, content, ts, "SENT");
                            System.out.println("✅ Message sent.");
                        }
                    } else {
                        System.out.println("Invalid choice.");
                    }
                } catch (Exception ex) {
                    System.out.println("❌ Error: " + ex.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Cannot connect to DB: " + e.getMessage());
        }
    }
}
