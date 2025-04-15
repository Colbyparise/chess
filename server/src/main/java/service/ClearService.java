package service;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;


public class ClearService {
    private AuthDAO authDao;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    public ClearService(AuthDAO authDao, GameDAO gameDAO, UserDAO userDAO) {
        this.authDao = authDao;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    public void clear() throws DataAccessException {
        authDao.clear();
        gameDAO.clear();
        userDAO.clear();
    }
}