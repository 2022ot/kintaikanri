package net.shinkasystems.kintai.service;

import java.sql.Date;
import java.util.Calendar;

import net.shinkasystems.kintai.KintaiDB;
import net.shinkasystems.kintai.entity.User;
import net.shinkasystems.kintai.entity.UserDao;
import net.shinkasystems.kintai.util.Authentication;
import net.shinkasystems.kintai.util.DaoFactory;

import org.seasar.doma.jdbc.tx.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 設定画面用サービスです。
 * 
 * @author Aogiri
 *
 */
public class ConfigService {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

	/**
	 * 指定のユーザーIDのパスワードを変更します。
	 * 
	 * @param userId パスワードを変更するユーザーID
	 * @param password パスワード（変更後）
	 */
	public void changePassword(int userId, String password) {

		Calendar expireCalendar = Calendar.getInstance();
		expireCalendar.add(Calendar.MONTH, 3);

		TransactionManager transactionManager = KintaiDB.singleton()
				.getTransactionManager();

		transactionManager.required(() -> {

			final UserDao dao = DaoFactory.createDaoImplements(UserDao.class);

			final User user = dao.selectById(userId);

			user.setPassword(new Authentication(user.getUserName(), password).getPasswordHash());
			user.setExpireDate(new Date(expireCalendar.getTimeInMillis()));

			dao.update(user);

			log.info("ユーザー設定を変更しました。" + user);

		});
	}
}
