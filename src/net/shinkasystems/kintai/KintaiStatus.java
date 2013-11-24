package net.shinkasystems.kintai;

/**
 * 申請情報のステータスを管理します。
 * @author Aogiri
 *
 */
public enum KintaiStatus {

	/**
	 * 「未決」のステータスです。
	 */
	PENDING("未決"),
	
	/**
	 * 「承認」のステータスです。
	 */
	APPROVED("承認"),
	
	/**
	 * 「却下」のステータスです。
	 */
	REJECTED("却下");
	
	/**
	 * 勤怠申請の表示用文字列です。
	 */
	public final String display;

	/**
	 * 
	 * @param display
	 */
	private KintaiStatus(String display) {
		this.display = display;
	}
}
