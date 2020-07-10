package be.vdab.jdbc;

import be.vdab.data.Pass;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Transactie {
    public static void main(String[] args) {
        // Driver declaratie toevoegen
        try {
            Class.forName( "com.mysql.cj.jdbc.Driver" );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String passw = new Pass().getPASSW();
        // Uit te voeren SQLs
        String query = "SELECT Stock FROM Beers WHERE Name =?";
        String update = "UPDATE Beers SET Stock =? WHERE Name =?";
        // Connectie met DB
        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/beersdb?serverTimezone=UTC"
                ,"root",passw);
        ) {
            System.out.println("Connectie ok");

            // Set DB-instellingen
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            // Prepare stmts
            try (
                    PreparedStatement qstmt = con.prepareStatement(query);
                    PreparedStatement ustmt = con.prepareStatement(update);
            ){
                String beer1 = "MyBeer";
                String beer2 = "test";
                int nwStock = 200;

                qstmt.setString(1,beer1);
                ResultSet rs = qstmt.executeQuery();
                rs.next();
                int oldStock = rs.getInt("Stock");
                System.out.println(beer1 + " heeft een stock van : " + oldStock);

                qstmt.setString(1,beer2);
                rs = qstmt.executeQuery();
                rs.next();
                oldStock = rs.getInt("Stock");
                System.out.println(beer2 + " heeft een stock van : " + oldStock);
                System.out.println("Update stmt uitvoeren...");

                // tx start
                System.out.println("tx start...");
                // update 1
                ustmt.setInt(1,nwStock);
                ustmt.setString(2,beer1);
                int aantalrec = ustmt.executeUpdate();
                // update 2
                ustmt.setInt(1,nwStock);
                ustmt.setString(2,beer2);
                aantalrec += ustmt.executeUpdate();
                // Controle update
                System.out.println("Update uitgevoerd, controle...");
                System.out.println("Aantal gewijzigde records : " + aantalrec);
                qstmt.setString(1,beer1);
                rs = qstmt.executeQuery();
                rs.next();
                int updStock = rs.getInt("Stock");
                System.out.println(beer1 + " heeft een stock van : " + updStock);
                qstmt.setString(1,beer2);
                rs = qstmt.executeQuery();
                rs.next();
                updStock = rs.getInt("Stock");
                System.out.println(beer2 + " heeft een stock van : " + updStock);
                // rollback
                System.out.println("Rollback...");
                // tx end
                con.rollback();
                System.out.println("tx end...");
            }
        }
        catch (Exception ex){
            System.out.println("Connectie nok");
            ex.printStackTrace(System.err);
        }
    }
}
