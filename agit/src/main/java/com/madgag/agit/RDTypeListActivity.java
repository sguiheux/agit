/*
 * Copyright (c) 2011 Roberto Tyley
 *
 * This file is part of 'Agit' - an Android Git client.
 *
 * Agit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Agit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.madgag.agit;

import static android.R.layout.simple_list_item_2;
import static com.google.inject.name.Names.named;
import static com.madgag.agit.GitIntents.OPEN_GIT_INTENT_PREFIX;
import static com.madgag.agit.R.id.actionbar;
import static com.madgag.agit.R.layout.list_activity_layout;
import static com.madgag.agit.RepoScopedActivityBase.enterRepositoryScopeFor;
import static com.madgag.android.listviews.ViewInflator.viewInflatorFor;

import org.eclipse.jgit.lib.Repository;

import roboguice.activity.RoboListActivity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.google.inject.Inject;
import com.google.inject.Key;
import com.madgag.agit.git.model.RepoDomainType;
import com.madgag.agit.guice.RepositoryScope;
import com.madgag.android.listviews.ViewFactory;
import com.madgag.android.listviews.ViewHolder;
import com.madgag.android.listviews.ViewHolderFactory;
import com.madgag.android.listviews.ViewHoldingListAdapter;
import com.markupartist.android.widget.ActionBar;

public class RDTypeListActivity<E> extends RoboListActivity {

	public static Intent listIntent(Repository repository, String typeName) {
		return new GitIntentBuilder(typeName + ".LIST").repository(repository)
				.toIntent();
	}

	private static final String TAG = "RDTL";
	private @Inject
	RepositoryContext rc;
	private @Inject
	Repository repository;
	private RepoDomainType<E> rdt;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		RepositoryScope repositoryScope = enterRepositoryScopeFor(this,
				getIntent());
		try {
			super.onCreate(savedInstanceState);
			rdt = extractRDTFromIntent();
		} finally {
			repositoryScope.exit();
		}

		setContentView(list_activity_layout);
		ActionBar actionBar = (ActionBar) findViewById(actionbar);
		actionBar.setHomeAction(new HomeAction(this));
		actionBar.setTitle(rdt.conciseSummaryTitle());

		setListAdapter(new ViewHoldingListAdapter<E>(rdt.getAll(),
				getViewFactory()));

		registerForContextMenu(getListView());

	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		startActivity(rdt.viewIntentFor((E) getListAdapter().getItem(position)));
	}

	/**
	 * Add Context Menu to use git command
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.gitoperation, menu);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Dialog dia = new Dialog(this);
		dia.setTitle(item.getTitle());
		dia.show();
		return super.onContextItemSelected(item);
	}

	private RepoDomainType<E> extractRDTFromIntent() {
		String rdtName = getIntent().getAction()
				.substring(OPEN_GIT_INTENT_PREFIX.length()).split("\\.")[0];
		return getInjector().getInstance(
				Key.get(RepoDomainType.class, named(rdtName)));
	}

	@Override
	protected void onResume() {
		super.onResume();
		rc.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		rc.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		rc.onDestroy();
	}

	public ViewFactory<E> getViewFactory() {
		return new ViewFactory<E>(viewInflatorFor(this, simple_list_item_2),
				new ViewHolderFactory<E>() {
					public ViewHolder<E> createViewHolderFor(View view) {
						return new RDTypeInstanceViewHolder(rdt, view);
					}
				});
	}
}
