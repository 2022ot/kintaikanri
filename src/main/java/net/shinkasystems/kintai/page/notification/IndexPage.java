package net.shinkasystems.kintai.page.notification;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;

import net.shinkasystems.kintai.KintaiConstants;
import net.shinkasystems.kintai.KintaiRole;
import net.shinkasystems.kintai.KintaiSession;
import net.shinkasystems.kintai.component.NotificationDataProvider;
import net.shinkasystems.kintai.component.StatusChoiceRenderer;
import net.shinkasystems.kintai.component.UserChoiceRenderer;
import net.shinkasystems.kintai.component.UserOption;
import net.shinkasystems.kintai.component.UserOptionUtility;
import net.shinkasystems.kintai.domain.NotificationStatus;
import net.shinkasystems.kintai.entity.sub.NotificationData;
import net.shinkasystems.kintai.page.DefaultLayoutPage;
import net.shinkasystems.kintai.panel.PaginationPanel;
import net.shinkasystems.kintai.service.notification.IndexService;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.yui.calendar.DatePicker;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * 申請情報の一覧画面です。 一覧画面は勤怠管理ツールのホーム画面でもあります。
 * 
 * アクセス可能な、ユーザー権限は以下の通りです。
 * <ul>
 * <li>管理ユーザー</li>
 * <li>一般ユーザー</li>
 * <li>期限切れ一般ユーザー</li>
 * </ul>
 * 
 * 期限切れ一般ユーザーには、パスワードの有効期限が切れている旨のメッセージが表示されます。
 * 
 * @author Aogiri
 * 
 */
@AuthorizeInstantiation({ KintaiRole.ADMIN, KintaiRole.USER, KintaiRole.EXPIRED_USER })
public class IndexPage extends DefaultLayoutPage {

	/** ロガー */
	private static final Logger log = LoggerFactory.getLogger(IndexPage.class);

	/**
	 * 単位ページあたりの申請情報の表示件数です。
	 */
	private static final int NUMBER_OF_ITEMS_PER_PAGE = 20;

	/**
	 * パラメータ「id」のキー名称です。
	 */
	public static final String PARAMETER_ID = "id";

	@Inject
	private IndexService indexService;

	/**
	 * 申請情報一覧のデータプロバイダーです。
	 */
	private NotificationDataProvider notificationDataProvider = new NotificationDataProvider(
			indexService,
			((KintaiSession) KintaiSession.get()).getUser().getId());

	/**
	 * 申請情報一覧です。
	 */
	private final DataView<NotificationData> notificationDataView = new DataView<NotificationData>(
			"index-data-view", notificationDataProvider, NUMBER_OF_ITEMS_PER_PAGE) {

		@Override
		protected void populateItem(Item<NotificationData> item) {

			final NotificationData notificationData = item.getModelObject();

			if (notificationData.getCommentApplycant().length() > 20) {
				notificationData.setCommentApplycant(notificationData.getCommentApplycant().substring(0, 20) + "...");
			}

			/*
			 * コンポーネントの生成
			 */
			final Label idLabel = new Label("id", notificationData.getId());
			final Label termLabel = new Label("term",
					notificationData.getTerm().format(DateTimeFormatter.ISO_LOCAL_DATE));
			final Label typeLabel = new Label("type", notificationData.getType().display);
			final Label commentLabel = new Label("comment", notificationData.getCommentApplycant());
			final Label dateLabel = new Label("date",
					notificationData.getCreateDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
			final Label applicantLabel = new Label("applicant", notificationData.getApplicantDisplayName());
			final Label statusLabel = new Label("status", notificationData.getStatus().display);

			final Link<String> typeLink = new StatelessLink<String>("link") {

				@Override
				public void onClick() {

					final PageParameters parameters = new PageParameters();

					parameters.set(PARAMETER_ID, notificationData.getId());

					IndexPage.this.setResponsePage(DetailPage.class, parameters);
				}

			};

			/*
			 * コンポーネントの編集
			 */
			typeLink.add(typeLabel);

			/*
			 * コンポーネントの組立
			 */
			item.add(idLabel);
			item.add(termLabel);
			item.add(typeLink);
			item.add(commentLabel);
			item.add(dateLabel);
			item.add(applicantLabel);
			item.add(statusLabel);
		}
	};

	/**
	 * 検索フォームです。
	 */
	private final Form<ValueMap> form = new Form<ValueMap>("search-form") {

		@Override
		protected void onError() {
			// TODO 自動生成されたメソッド・スタブ
			super.onError();
		}

		@Override
		protected void onSubmit() {

			java.sql.Date from = null;
			java.sql.Date to = null;
			Integer applicantId = null;
			NotificationStatus status = null;

			if (fromTextField.getModelObject() != null) {
				from = new java.sql.Date(fromTextField.getModelObject().getTime());
			}
			if (toTextField.getModelObject() != null) {
				to = new java.sql.Date(toTextField.getModelObject().getTime());
			}
			if (userDropDownChoice.getModelObject() != null) {
				applicantId = userDropDownChoice.getModelObject().getId();
			}
			if (statusDropDownChoice.getModelObject() != null) {
				status = statusDropDownChoice.getModelObject();
			}

			notificationDataProvider.setFrom(from);
			notificationDataProvider.setTo(to);
			notificationDataProvider.setApplicantId(applicantId);
			notificationDataProvider.setStatus(status);
			notificationDataProvider.setSort("term", SortOrder.DESCENDING);
		}

	};

	/**
	 * 「From」の入力フィールドです。
	 */
	private final DateTextField fromTextField = new DateTextField("from", new Model<Date>(),
			KintaiConstants.DATE_PATTERN);

	/**
	 * 「To」の入力フィールドです。
	 */
	private final DateTextField toTextField = new DateTextField("to", new Model<Date>(), KintaiConstants.DATE_PATTERN);

	/**
	 * 申請者の選択コンポーネントです。
	 */
	private final DropDownChoice<UserOption> userDropDownChoice = new DropDownChoice<UserOption>(
			"applicant", new Model<UserOption>(), UserOptionUtility.getUserOptions(), new UserChoiceRenderer());

	/**
	 * ステータスの選択コンポーネントです。
	 */
	private final DropDownChoice<NotificationStatus> statusDropDownChoice = new DropDownChoice<NotificationStatus>(
			"status", new Model<NotificationStatus>(), Arrays.asList(NotificationStatus.values()),
			new StatusChoiceRenderer());

	/**
	 * コンストラクタです。
	 */
	public IndexPage() {
		super();

		/*
		 * コンポーネントの生成
		 */
		final OrderByBorder<String> termOrderByBorder = new OrderByBorder<String>("orderByTerm", "TERM",
				notificationDataProvider) {

			@Override
			protected void onSortChanged() {
				notificationDataView.setCurrentPage(0);
			}

		};
		final PagingNavigator pagingNavigator = new PaginationPanel("page-navigator", notificationDataView);

		/*
		 * コンポーネントの編集
		 */
		fromTextField.add(new DatePicker());
		toTextField.add(new DatePicker());

		userDropDownChoice.setNullValid(true);
		statusDropDownChoice.setNullValid(true);

		/*
		 * コンポーネントの組立
		 */
		add(termOrderByBorder);
		add(notificationDataView);
		add(pagingNavigator);

		form.add(fromTextField);
		form.add(toTextField);
		form.add(userDropDownChoice);
		form.add(statusDropDownChoice);

		add(form);
	}
}
