package org.flowkeeper.server.api;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXB;

import org.flowkeeper.server.UserType;
import org.flowkeeper.server.Users;

/**
 *
 * 
 */
public class RestfulServerImpl extends GenericServer {

    public RestfulServerImpl(String userid, String password) throws IOException, LoginException {
        // TODO: Connect to the real server here
        InputStream is = getClass().getClassLoader().getResourceAsStream("users.xml");
        try {
            users = JAXB.unmarshal(is, Users.class);
            for (UserType u: users.getUser()) {
                if (u.getId().equals(userid) && u.getPassword().equals(password)) {
                    user = u;
                    return;
                }
            }
            throw new LoginException();
        } finally {
            is.close();
        }
    }

    public void logout() {
    }

    public void sendMessage(String userId, String message) throws InvalidStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public void flush() {
		// Do nothing, ignore
	}

}
