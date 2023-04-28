import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        boolean online = true;

        System.out.println("Hello Sqlite!");
        String dbName = "test";
        Connection conn = getConnected(dbName);

        if (conn != null) {
            System.out.println("Databasen " + dbName + " är live!");
            try {
                createTable(conn);
            } catch (SQLException e) {
                System.out.println("Något gick snett med createTable");
                System.out.println(e.getMessage());
            }
        }

        while(online){

            if (conn != null) { // Ifall databasen inte är connected, körs inte programmet
                autoUpdateId(conn);
            }

            System.out.println("1.Add person");
            System.out.println("2.Delete person");
            System.out.println("3.Update person");
            System.out.println("4.Print table");
            System.out.println("5.Reset table");
            System.out.println("6.Exit");
            System.out.print("Your choice: ");
            int choice = scan.nextInt();

            switch (choice){
                /* case 1 -> createPerson(conn, scan);     ALTERNATIV switch
                case 2 -> deletePerson(conn, scan);
                case 3 -> updatePerson(conn, scan);
                case 4 -> {
                    System.out.println();
                    printPeopleInDatabase(conn);
                    System.out.println();
                }
                case 5 -> resetTable(conn);
                case 6 -> online = false;
                */

                case 1:
                    createPerson(conn, scan);
                    break;

                case 2:
                    deletePerson(conn, scan);
                    break;

                case 3:
                    updatePerson(conn, scan);
                    break;

                case 4:
                    printPeopleInDatabase(conn);
                    break;

                case 5:
                    resetTable(conn);
                    break;

                case 6:
                    online = false;
                    break;
            }
        }
        scan.close();
    }

    private static Connection getConnected (String dbName){
        Connection conn = null; // När vi kopplar upp oss skapas en session som hålls levande tills man stänger den man måste stänga den annars överhettas servern!
        try { // Skyddar mot krasch
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbName + ".db"); // Öppnar en anslutning till en databas
        }
        catch (SQLException e) { // fångar error i databaskopplingen
            // throw new RuntimeException(e);
            System.out.println("Något gick snett med databaskopplingen");
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static boolean isTableEmpty(Connection conn) {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM persons");
            if (rs.next()) {
                int count = rs.getInt(1);
                return count == 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if table is empty: " + e.getMessage());
        }
        return false;
    }


    private static void createTable (Connection conn) throws SQLException{
        Statement stmt = conn.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS persons (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name varchar(50)," +
                "lastName VARCHAR(50)," +
                "age integer" +
                ")";
        stmt.execute(sql);

    }

    private static void autoUpdateId(Connection conn){
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE persons SET id = (SELECT COUNT(*) FROM persons p WHERE p.id < persons.id) + 1");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private static void createPerson(Connection conn, Scanner scan) {
        String sql = "INSERT INTO persons (name,lastname,age) " + "VALUES (?,?,?)"; // Första två ? ? är strängar, skriv ej '?' då blir det fel
        // skriv ej VALUES ('" + name + "', '" + lastName + "', '" + age + ")"; p.g.a sql injections

        try {
            System.out.println();
            System.out.print("Enter name: ");
            String name = scan.next();
            System.out.print("Enter lastname: ");
            String lastName = scan.next();
            System.out.print("Enter age: ");
            int age = scan.nextInt();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name); // Första parametern ska vi ha name
            ps.setString(2, lastName); // Andra parametern ska vi ha lastName
            ps.setInt(3, age); // Tredje parametern ska vi ha age
            // ps.setString(3, String.valueOf(age)); // Kan även skrivas såhär
            ps.executeUpdate();
            System.out.println();

        } catch (SQLException e) {
            System.out.println("Något gick snett med createPerson");
            System.out.println(e.getMessage());
        }
    }


    private static void deletePerson (Connection conn, Scanner scan) {
        printPeopleInDatabase(conn); // Print databas, order by name
        System.out.print("Ange id du vill radera: ");
        int id = scan.nextInt(); // id-värde du vill radera
        PreparedStatement ps = null;   // Radera en rad från tabellen baserat på dess id
        try {
            ps = conn.prepareStatement("DELETE FROM persons WHERE id = ?");
            ps.setInt(1, id);
            int result = ps.executeUpdate();

            if (result > 0) {
                System.out.println("Rad med id " + id + " raderad från databasen.");
            } else {
                System.out.println("Ingen rad med id " + id + " hittades i databasen.");
            }

        } catch (SQLException e) {
            System.out.println("Något gick snett med deletePerson");
            System.out.println(e.getMessage());
        }
    }

    private static void updatePerson (Connection conn, Scanner scan) { // Uppdatera en rad i tabellen baserat på dess id
        printPeopleInDatabase(conn); // Print databas, order by name

        System.out.print("Enter id to update: ");
        int id = scan.nextInt(); // id-värde du vill uppdatera
        System.out.print("Enter name: ");
        String name = scan.next(); // namn du vill uppdatera
        System.out.print("Enter lastname: ");
        String lastName = scan.next(); // lastnamn du vill uppdatera
        System.out.print("Enter age: ");
        int age = scan.nextInt(); // age du vill uppdatera

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("UPDATE persons SET name = ?, lastname = ?, age = ? WHERE id = ?");
            ps.setString(1, name);
            ps.setString(2, lastName);
            ps.setString(3, String.valueOf(age));
            ps.setInt(4, id);
            int result = ps.executeUpdate();

            if (result > 0) {
                System.out.println("Rad med id " + id + " uppdaterad i databasen.");
            }
            else {
                System.out.println("Ingen rad med id " + id + " hittades i databasen.");
            }

        }
        catch (SQLException e) {
            System.out.println("Något gick snett med updatePerson");
            System.out.println(e.getMessage());
        }

    }



    private static void printPeopleInDatabase(Connection conn) {
        PreparedStatement ps = null;
        if(isTableEmpty(conn)){
            System.out.println("Table is empty" + "\n");
        }
        else{

            try {
                System.out.println();
                ps = conn.prepareStatement("SELECT id, name, lastName, age FROM persons ORDER BY name");
                ResultSet rs = ps.executeQuery();// kopierar info från db till rs
                System.out.println("Namn sorterade efter förnamn: ");

                while (rs.next()) {
                    String id = rs.getString("id");
                    String name = rs.getString("name");
                    String lastName = rs.getString("lastName");
                    String age = rs.getString("age");
                    System.out.println(id + " " + name + " " + lastName + " " + age);
                    System.out.println();

                    // alternativt
                    // System.out.println(rs.getString("name") + " " + rs.getString("lastName") + " " + rs.getString("age"));

                }
            } catch (SQLException e) {
                System.out.println("Något gick snett med printDatabas");
                System.out.println(e.getMessage());
            }
        }
        }




    private static void resetTable(Connection conn) {

        PreparedStatement ps = null;   // Radera en rad från tabellen baserat på dess id
        try {
            ps = conn.prepareStatement("DELETE FROM persons");
            int result = ps.executeUpdate();

            if (result > 0) {
                System.out.println("All info har raderats ifrån databasen." + "\n");
            }

        } catch (SQLException e) {
            System.out.println("Något gick snett med resetTable");
            System.out.println(e.getMessage());
        }

    }
    }
