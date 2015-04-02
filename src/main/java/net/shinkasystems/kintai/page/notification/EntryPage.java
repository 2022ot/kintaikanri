package net.shinkasystems.kintai.page.notification;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.shinkasystems.kintai.KintaiConstants;
import net.shinkasystems.kintai.KintaiRole;
import net.shinkasystems.kintai.KintaiSession;
import net.shinkasystems.kintai.component.NotificationTypeChoiceRendere;
import net.shinkasystems.kintai.component.UserChoiceRenderer;
import net.shinkasystems.kintai.component.UserOption;
import net.shinkasystems.kintai.component.UserOptionUtility;
import net.shinkasystems.kintai.domain.NotificationType;
import net.shinkasystems.kintai.entity.Notification;
import net.shinkasystems.kintai.entity.User;
import net.shinkasystems.kintai.mail.Mailer;
import net.shinkasystems.kintai.page.DefaultLayoutPage;
import net.shinkasystems.kintai.panel.AlertPanel;
import net.shinkasystems.kintai.service.notification.EntryService;
import net.shinkasystems.kintai.util.DateUtils;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * 申請情報の登録画面です。
 * 
 * @author Aogiri
 * 
 */
@AuthorizeInstantiation({ KintaiRole.ADMIN, KintaiRole.USER })
public class EntryPage extends DefaultLayoutPage {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(EntryPage.class);

	/**
	 * アラートパネルです。
	 */
	private final Panel alertPanel = new AlertPanel("alert-panel");

	/**
	 * 申請を行うフォームです。
	 */
	private final Form<ValueMap> entryForm = new Form<ValueMap>("entry-form") {

		@Override
		protected void onValidate() {

			alertPanel.setVisible(true);
		}

		@Override
		protected void onSubmit() {

			/*
			 * 申請処理
			 */
			final Notification notification = new Notification();

			if (applicantDropDownChoice.getModelObject() != null) {
				notification.setApplicantId(applicantDropDownChoice.getModelObject().getId());
				notification.setProxyId(((KintaiSession) KintaiSession.get()).getUser().getId());
			} else {
				notification.setApplicantId(((KintaiSession) KintaiSession.get()).getUser().getId());
			}
			notification.setType(typeDropDownChoice.getModelObject());
			notification.setTerm(DateUtils.toLocalDate(termTextField.getModelObject()));
			notification.setCommentApplycant(commentTextArea.getModelObject());

			entryService.entry(notification);

			/*
			 * 詳細ページの URL を作成する
			 */
			PageParameters pageParameters = new PageParameters();
			pageParameters.set("id", notification.getId());

			String urlString = RequestCycle.get().getUrlRenderer().renderFullUrl(
					Url.parse(urlFor(DetailPage.class, pageParameters).toString()));

			/*
			 * メール送信処理
			 */
			final User sender = entryService.getUser(notification.getApplicantId());
			final User receiver = entryService.getUser(sender.getAuthorityId());

			Mailer.NOTIFICATION_ENTRY.send(
					receiver.getEmailAddress(),
					sender.getEmailAddress(),
					receiver.getDisplayName(),
					sender.getDisplayName(),
					notification.getTerm().format(DateTimeFormatter.ISO_LOCAL_DATE),
					notification.getType().display,
					notification.getCommentApplycant(),
					urlString);

			setResponsePage(IndexPage.class);
		}

	};

	/**
	 * 勤怠申請の種類を管理するドロップダウンです。
	 */
	private final DropDownChoice<NotificationType> typeDropDownChoice = new DropDownChoice<NotificationType>(
			"type",
			new Model<NotificationType>(),
			Arrays.asList(NotificationType.values()),
			new NotificationTypeChoiceRendere());

	/**
	 * 期日の入力フィールドです。
	 */
	private final DateTextField termTextField = new DateTextField("term", new Model<java.util.Date>(),
			KintaiConstants.DATE_PATTERN);

	/**
	 * 事由等のコメントを入力するテキストエリアです。
	 */
	private final TextArea<String> commentTextArea = new TextArea<>("comment", new Model<String>());

	/**
	 * 代理申請が可能な申請者のリストです。
	 */
	private final List<UserOption> applicantOptions = new ArrayList<UserOption>();

	/**
	 * 代理申請を行う場合の申請者を管理するドロップダウンです。
	 */
	private final DropDownChoice<UserOption> applicantDropDownChoice = new DropDownChoice<UserOption>(
			"applicant", new Model<UserOption>(), applicantOptions, new UserChoiceRenderer());

	@Inject
	EntryService entryService;

	/**
	 * コンストラクタです。
	 */
	public EntryPage() {
		super();

		/*
		 * コンポーネントの編集
		 */
		alertPanel.setVisible(false);
		typeDropDownChoice.setRequired(true);
		termTextField.setRequired(true);
		termTextField.add(new DatePicker());
		commentTextArea.setRequired(true);

		applicantOptions.addAll(UserOptionUtility.getUserOptions(
				((KintaiSession) KintaiSession.get()).getUser().getId()));
		if (applicantOptions.size() > 0) {
			applicantDropDownChoice.setEnabled(true);
		} else {
			applicantDropDownChoice.setEnabled(false);
		}

		/*
		 * コンポーネントの組立
		 */
		entryForm.add(typeDropDownChoice);
		entryForm.add(termTextField);
		entryForm.add(commentTextArea);
		entryForm.add(applicantDropDownChoice);

		add(alertPanel);
		add(entryForm);
	}
}