package net.shinkasystems.kintai;

import java.util.Date;

import net.shinkasystems.kintai.entity.User;
import net.shinkasystems.kintai.entity.UserDao;
import net.shinkasystems.kintai.entity.UserDaoImpl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.seasar.doma.jdbc.tx.LocalTransaction;

/**
 * 勤怠管理ツールの認証セッションを管理します。
 * 
 * @author Aogiri
 * 
 */
public class KintaiSession extends AuthenticatedWebSession {

	private User user;

	private Roles roles;

	public KintaiSession(Request request) {
		super(request);
	}

	@Override
	public boolean authenticate(String username, String password) {

		String hashedPassword = DigestUtils.sha512Hex(password);

		LocalTransaction transaction = KintaiDB.getLocalTransaction();
		try {
			transaction.begin();

			UserDao dao = new UserDaoImpl();

			User user = dao.selectByUserName(username);

			transaction.commit();
		} finally {
			transaction.rollback();
		}



		if (user == null || user.getPassword() != hashedPassword) {
			return false;
		} else {

			this.user = user;

			if (new Date().after(user.getExpireDate())) {
				roles = KintaiRole.EXPIRED_USER.getWicketRoles();
			} else if (KintaiRole.USER.equals(user.getRole())) {
				roles = KintaiRole.USER.getWicketRoles();
			} else if (KintaiRole.ADMIN.equals(user.getRole())) {
				roles = KintaiRole.ADMIN.getWicketRoles();
			}

			return true;
		}
	}

	@Override
	public Roles getRoles() {

		if (isSignedIn()) {
			return roles;
		} else {
			return null;
		}
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setRoles(Roles roles) {
		this.roles = roles;
	}

}
