package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles everything that deals with any actions of a user outside of his account
 * such as registering, login, and loading and saving of users to the master list.
 * @author Gabriel Pua
 * @author Jared Sy
 * @version 2.0
 */
public class UserSystem {
    /** All the registered usernames. */
    private static ArrayList<String> usernames;
    /** The roles of the registered users. */
    private static ArrayList<String> roles;
    /** The data of each user. */
    private static ArrayList<Citizen> users;
    /** The visit records of each user. */
    private static ArrayList<ArrayList<Visit>> records;
    /** The list of positive cases. */
    private static ArrayList<Case> cases;

    /**
     * Returns the index of the given username in the list of registered usernames. If
     * the username is not found, returns -1.
     * @param username the name to be checked in the master list.
     * @return the index of the username in the list, and -1 if it is not in the list.
     */
    public static int getIndexOf(String username) {
        return usernames.indexOf(username.toUpperCase());
    }

    /**
     * Returns the username of a specific user in the system given his index.
     * @param index the index of a specific user in the system.
     * @return the username of the user.
     */
    public static String getUsername(int index) {
        return usernames.get(index);
    }

    /**
     * Returns the role of a specific user in the system given his index.
     * @param index the index of a specific user in the system.
     * @return the role of the user.
     */
    public static String getRoleOf(int index) {
        return roles.get(index);
    }

    /**
     * Returns the user given a specified username. If the username
     * is not found in the master list, it returns null.
     * @param username the name to be checked in the master list.
     * @return the user with the specified username if found, null otherwise.
     */
    public static Citizen getUser(String username) {
        int index = getIndexOf(username);
        if (index != -1)
            return users.get(index);
        else
            return null;
    }

    /**
     * Returns the last user to be added in the system.
     * @return the last user to be added in the system.
     */
    public static Citizen getLastUser() {
        return users.get(users.size() - 1);
    }

    /**
     * Returns the visit records of each user.
     * @return the visit records of each user.
     */
    public static ArrayList<ArrayList<Visit>> getRecords() {
        return records;
    }

    /**
     * Returns the list of cases.
     * @return the list of cases.
     */
    public static ArrayList<Case> getCases() {
        return cases;
    }

    /**
     * Returns an ArrayList of the usernames of all registered contact tracers.
     * @return an ArrayList of the usernames of all registered contact tracers in the system.
     */
    public static ArrayList<String> getTracers() {
        ArrayList<String> tracers = new ArrayList<>();
        for (Citizen i: users)
            if (getRoleOf(getIndexOf(i.getUsername())).equals("tracer"))
                tracers.add(i.getUsername());
        return tracers;
    }

    /**
     * Checks if the username has not been taken, is not empty, and does not have a space.
     * @param username the username.
     * @return true if the username is valid, false otherwise.
     */
    public static boolean isValidNewUsername(String username) {
        return getIndexOf(username.toUpperCase()) == -1 && !username.isBlank() && !username.contains(" ");
    }

    /**
     * Adds the citizen object to the database.
     * @param citizen the new user to add to the database.
     */
    public static void register(Citizen citizen) {
        // add username, role, and information to ArrayList and add new ArrayList for visit records
        usernames.add(citizen.getUsername());
        roles.add("citizen");
        users.add(citizen);
        records.add(new ArrayList<>());
    }

    /**
     * Handles the logging in of an existing user. Based on the username and password,
     * determines if they are valid. Once valid, loads everything from the user's
     * information in the system.
     * @param username the username of the user logging in.
     * @param password the password of the user logging in.
     * @return a constructed Citizen object with the user's details.
     */
    public static Citizen login(String username, String password) {
        int index = getIndexOf(username.toUpperCase());
        System.out.println(index);

        if (index != -1 && users.get(index).getPassword().equals(password)) {
            switch (getRoleOf(index)) {
                case "citizen":
                    return new Citizen(users.get(index));

                case "official":
                    return new GovOfficial(users.get(index));

                case "tracer":
                    return new Tracer(users.get(index));
            }
        }

        return null;
    }

    /**
     * Checks if the String input is a valid password. Returns true if valid
     * and false otherwise.
     * @param pass the String to be checked.
     * @return true if it is valid, false otherwise.
     */
    public static boolean isValidPassword(String pass) {
        Matcher matcher = Pattern.compile("[\\W\\d]").matcher(pass.replaceAll("\\s+", ""));

        return pass.length() >= 6 && matcher.find() && !pass.contains(" ");
    }

    /**
     * Sets a new role for a specific user in the system given his index.
     * @param index the index of a specific user in the system.
     * @param role the new role to be assigned to the user.
     */
    public static void setRoleOf(int index, String role) {
        if (roles.get(index).equals("tracer")) { // previous role is tracer
            Tracer temp = (Tracer) users.get(index);
            temp.demote();

            // set all pending cases assigned to contact tracer to default
            for (Case i: cases) {
                if (i.getTracer().equals(temp.getUsername()) && i.getStatus() != 'T') {
                    i.setTracer("000");
                }
            }
        }

        roles.set(index, role);
    }

    /**
     * Replaces the object in the system with the new and updated one.
     * @param citizen the object to replace the one in the system.
     */
    public static void updateUser(Citizen citizen) {
        users.set(usernames.indexOf(citizen.getUsername()), citizen);
    }

    /**
     * Removes the last user in the system as well as the corresponding role, username,
     * and visit records.
     */
    public static void removeLastUser() {
        int index = usernames.size() - 1;
        usernames.remove(index);
        users.remove(index);
        roles.remove(index);
        records.remove(index);
    }

    /**
     * Adds a visit record to the list of visit records in the system.
     * @param estCode the establishment code of the visit record.
     * @param date the date when the visit was made.
     * @param username the name to be checked in the master list.
     */
    public static void addRecord(String estCode, Calendar date, String username) {
        // get machine time
        Calendar time = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
        date.set(Calendar.MINUTE, time.get(Calendar.MINUTE));

        records.get(getIndexOf(username)).add(new Visit(estCode, date));
    }

    /**
     * Adds a case to the list of cases in the system.
     * @param username the username of the user who reported positive.
     * @param date the date when the case is reported.
     */
    public static void addCase(String username, Calendar date) {
        cases.add(new Case(username, date));
    }

    /**
     * Initializes all the ArrayLists from the text files.
     * This assumes that the following files already exist:<br>
     * Master_List.txt - the master list containing all the usernames and roles.<br>
     * Establishment_Records.txt - the text file containing all the check in records of the
     * users. The records listed in this file are assumed to be complete and follow the order
     * of the users in the master list i.e. this file first lists all the records of the first
     * user in the master list, then all the records of the second user, and so on.<br>
     * Positive_Cases.txt - the list of all the positive cases reported by the users. The
     * cases listed in this file are assumed to sorted i.e. the first case is case number 1,
     * followed by case number 2, and so on.<br>
     * Respective .act files - contains the information of a user.<br>
     *
     * At the minimum, the Master_List.txt and the respective .act file for each user must exist.
     */
    public static void loadSystem() {
        usernames = new ArrayList<>();
        roles = new ArrayList<>();
        users = new ArrayList<>();
        records= new ArrayList<>();
        cases = new ArrayList<>();

        String[] info;

        // load master list along with citizen accounts
        try (Scanner input = new Scanner(new File("Master_List.txt"))) {
            do {
                info = input.nextLine().split(" ");
                usernames.add(info[0]);
                roles.add(info[1]);
                try (Scanner reader = new Scanner(new File(info[0]+ ".act"))) {
                    String password = reader.nextLine();
                    String[] name = reader.nextLine().split(",");
                    String homeAdd = reader.nextLine().substring(5);
                    String officeAdd = reader.nextLine().substring(7);
                    String phoneNumber = reader.nextLine().substring(6);
                    String email = reader.nextLine().substring(6);

                    users.add(new Citizen(new Name(name[0], name[1], name[2]), homeAdd,
                                    officeAdd, phoneNumber, email, info[0], password, false));
                } catch (Exception e) {
                    System.out.println("User file not found: " + info[0] + ".act!");
                    e.printStackTrace();
                }
            } while (input.hasNextLine());
        } catch (FileNotFoundException e) {
            usernames.add("ADMIN2020");
            users.add(new Citizen(new Name("Admin", "", "Gov"), "Malacañang Palace",
                    "City Hall", "09123456789", "admin@gov.ph", "ADMIN2020", "@Dm1n0202", true));
            roles.add("official");
            System.out.println("Error! Master list not found.\nCreating default admin...");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar.Builder builder = new Calendar.Builder();
        String[] date;
        // load establishment records
        try (Scanner input = new Scanner(new File("Establishment_Records.txt"))) {
            String temp;
            int i = 0, time;
            records.add(new ArrayList<>());
            input.nextLine(); // read username ADMIN2020
            while (input.hasNextLine()) {
                temp = input.nextLine();
                if (temp.isEmpty()) {
                    if (input.hasNextLine() && !input.nextLine().isEmpty()) {
                        records.add(new ArrayList<>());
                        i++;
                    } else {
                        break;
                    }
                } else {
                    info = temp.split("\\s+");
                    date = info[1].split(",");
                    time = Integer.parseInt(info[2]);
                    builder.setDate(Integer.parseInt(date[2]), Integer.parseInt(date[0]) - 1, Integer.parseInt(date[1]));
                    builder.setTimeOfDay(time / 100, time % 100, 0);

                    records.get(i).add(new Visit(info[0], builder.build()));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Establishment_Records.txt not found. No data to load.");
            for (int i = 0; i < usernames.size(); i++) {
                records.add(new ArrayList<>());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // load positive cases
        try (Scanner input = new Scanner(new File("Positive_Cases.txt"))) {
            while (input.hasNextLine()) {
                info = input.nextLine().split(" ");
                date = info[2].split(",");
                builder.setDate(Integer.parseInt(date[2]), Integer.parseInt(date[0]) - 1, Integer.parseInt(date[1]));

                Citizen temp = getUser(info[1]); // get the positive user
                if (temp != null) {
                    temp.reportPositive(builder.build()); // set isPositive to true
                    int index = Integer.parseInt(info[0]) - 1;
                    cases.get(index).setTracer(info[3]);
                    cases.get(index).setStatus(info[4].charAt(0));

                    // case is assigned to a tracer and status is traced
                    if (!info[3].equals("000") && info[4].charAt(0) == 'T') {
                        Tracer tracer = new Tracer(users.get(0)); // create new Tracer object
                        // finish tracing for the case
                        tracer.addCase(cases.get(index));
                        tracer.trace(index + 1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Positive_Cases.txt not found. No data to load.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when terminating the program to free all the memory used and save
     * the system data in the appropriate text files.
     *
     * This writes the following files with the assigned file names:<br>
     * Master_List.txt - the master list containing all the usernames and roles.<br>
     * Establishment_Records.txt - the text file containing all the check in records of the users.<br>
     * Positive_Cases.txt - the list of all the users who reported positive.<br>
     * Respective .act files - contains the information of a user.<br>
     */
    public static void exitSystem() {
        // Update Master List
        try (FileWriter masterList = new FileWriter("Master_List.txt", false)) {
            for (int i = 0; i < usernames.size(); i++) {
                masterList.write(usernames.get(i) + " " + roles.get(i) + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update Account information
        for (Citizen citizen : users) {
            if (citizen.getIsChanged()) {
                try (FileWriter accFile = new FileWriter(citizen.getUsername().concat(".act"), false)) {
                    accFile.write(citizen.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Update Establishment Records
        try (FileWriter recFile = new FileWriter("Establishment_Records.txt", false)) {
            int i = 0;
            // get date 30 days ago
            Calendar temp = Calendar.getInstance();
            temp.add(Calendar.DAY_OF_YEAR, -30);

            for (ArrayList<Visit> user : records) {
                recFile.write(usernames.get(i++) + "\n");
                for (Visit visit : user) {
                    if (!visit.getCheckIn().before(temp))
                        recFile.write(visit.toString() + "\n");
                }
                recFile.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Update Positive Cases
        try (FileWriter posFile = new FileWriter("Positive_Cases.txt", false)) {
            for (Case i : cases) {
                posFile.write(i.toString() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        usernames = null;
        roles = null;
        users = null;
        records = null;
        cases = null;

        System.gc();
    }
}
