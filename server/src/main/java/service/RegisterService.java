package service;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.DatabaseException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.UserDAO;

import model.UserData;
import network.http.Register;
import network.http.RegisterResult;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterService {

    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public RegisterService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(Register req) throws DataAccessException {
        var newUser = req.userData();

        if (isInvalid(newUser)) {
            throw new BadRequestException("Error: bad request");
        }

        if (userDAO.getUser(newUser.username()) != null) {
            throw new DatabaseException("Error: already taken");
        }

        var hashedPassword = BCrypt.hashpw(newUser.password(), BCrypt.gensalt());
        var securedUser = new UserData(newUser.username(), hashedPassword, newUser.email());

        userDAO.createUser(securedUser);

        var authToken = AuthDAO.generateAuth(securedUser.username());
        authDAO.createAuth(authToken);

        return new RegisterResult(securedUser.username(), authToken.authToken());
    }

    private boolean isInvalid(UserData user) {
        return user == null || user.username() == null || user.password() == null || user.email() == null;
    }
}
