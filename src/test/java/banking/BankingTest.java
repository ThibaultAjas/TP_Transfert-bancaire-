package banking;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;

public class BankingTest {

    private static DataSource myDataSource; // La source de données à utiliser
    private static Connection myConnection;
    private BankingDAO myDAO;

    @Before
    public void setUp() throws SQLException, IOException, SqlToolError {
        // On crée la connection vers la base de test "in memory"
        myDataSource = getDataSource();
        myConnection = myDataSource.getConnection();
        // On initialise la base avec le contenu d'un fichier de test
        String sqlFilePath = BankingTest.class.getResource("testdata.sql").getFile();
        SqlFile sqlFile = new SqlFile(new File(sqlFilePath));
        sqlFile.setConnection(myConnection);
        sqlFile.execute();
        sqlFile.closeReader();
        // On crée l'objet à tester
        myDAO = new BankingDAO(myDataSource);
    }

    @After
    public void tearDown() throws SQLException {
        myConnection.close();
        myDAO = null; // Pas vraiment utile
    }

    @Test
    public void findExistingCustomer() throws SQLException {
        float balance = myDAO.balanceForCustomer(0);
        // attention à la comparaison des nombres à virgule flottante !
        // Le dernier paramètre correspond à la marge d'erreur tolérable
        assertEquals("Balance incorrecte !", 100.0f, balance, 0.001f);
    }

    @Test
    public void successfulTransfer() throws Exception {
        float amount = 10.0f;
        int fromCustomer = 0; // Le client 0 dispose de 100€ dans le jeu de tests
        int toCustomer = 1;
        // On mémorise les balances dans les deux comptes avant la transaction
        float before0 = myDAO.balanceForCustomer(fromCustomer);
        float before1 = myDAO.balanceForCustomer(toCustomer);
        // On exécute la transaction, qui doit réussir
        myDAO.bankTransferTransaction(fromCustomer, toCustomer, amount);
        // Les balances doivent avoir été mises à jour dans les 2 comptes
        assertEquals("Balance incorrecte !", before0 - amount, myDAO.balanceForCustomer(fromCustomer), 0.001f);
        assertEquals("Balance incorrecte !", before1 + amount, myDAO.balanceForCustomer(toCustomer), 0.001f);
    }

    @Test(expected = InsufficientBalanceException.class)
    public void negativeCustomerAccount() throws Exception {
        float amount = 500.0f;
        int fromCustomer = 0; // Il ne dispose apparement que de 100€
        int toCustomer = 1;
        myDAO.bankTransferTransaction(fromCustomer, toCustomer, amount);
        // On pourrait modifier le DAO, mais le SQL gère lui-même l'erreur,
        // et leve une exception dans le JAVA, ce qui effectue un ROLLBACK
        // (dans le catch)
        // 
        // EDIT: Au final je trouve ça plus propre de gérer ca directement dans le Java
    }
    
    @Test(expected = NonExistentCustomerException.class)
    public void innexistantCustomerAccount() throws Exception {
        float amount = 50.0f;
        int fromCustomer = 42;  // Il n'existe pas
        int toCustomer = 43;    // Lui non plus
        myDAO.bankTransferTransaction(fromCustomer, toCustomer, amount);
    }

    public static DataSource getDataSource() throws SQLException {
        org.hsqldb.jdbc.JDBCDataSource ds = new org.hsqldb.jdbc.JDBCDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:testcase;shutdown=true");
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }
}
