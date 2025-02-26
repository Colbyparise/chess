//package service;
//
//import dataAccess.*;
//
//import model.AuthData;
//
//import model.UserData;
//
//import java.util.UUID;
//
//public class UserService {
//    UserDAO userDAO;
//    AuthDAO authDAO;
//
//    public UserService(UserDAO userDAO, AuthDAO authDAO) {
//        this.userDAO = userDAO;
//        this.authDAO = authDAO;
//    }
//
//    public AuthDAO createUser(UserData userData) throws BadRequestException {
//
//        try {
//            userDAO.createUser(userData);
//        } catch (DataAccessException exception) {
//            throw new BadRequestException(exception.getMessage());
//    }
//}
//}