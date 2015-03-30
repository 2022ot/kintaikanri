package net.shinkasystems.kintai.entity.sub;

import java.io.Serializable;

import net.shinkasystems.kintai.entity.Notification;
import net.shinkasystems.kintai.entity.NotificationListener;

import org.seasar.doma.Column;
import org.seasar.doma.Entity;

/**
 * 勤怠情報一覧の表示用データを管理します。
 * 
 * @author Aogiri
 *
 */
@Entity(listener = NotificationListener.class)
public class NotificationData extends Notification implements Serializable {

	/**
	 * 申請者の表示名です。
	 */
	@Column(name = "APPLICANT_DISPLAY_NAME")
	private String applicantDisplayName;

	/**
	 * 
	 * @return
	 */
	public String getApplicantDisplayName() {
		return applicantDisplayName;
	}

	/**
	 * 
	 * @param applicantDisplayName
	 */
	public void setApplicantDisplayName(String applicantDisplayName) {
		this.applicantDisplayName = applicantDisplayName;
	}

}
