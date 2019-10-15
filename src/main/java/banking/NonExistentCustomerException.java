/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package banking;

/**
 *
 * @author Dalfrak
 */
public class NonExistentCustomerException extends Exception {
    
    public NonExistentCustomerException(String msg) {
        super(msg);
    }
    
}
