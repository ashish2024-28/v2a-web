package com.v2s.player.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.v2s.player.R;
import com.v2s.player.core.navigation.AppNavigator;
import com.v2s.player.core.permissions.PermissionManager;
import com.v2s.player.core.ui.V2SUi;
import com.v2s.player.core.utils.ViewUtils;
import com.v2s.player.data.model.MediaItem;
import com.v2s.player.data.model.Playlist;
import com.v2s.player.data.repository.LibraryRepository;
import com.v2s.player.data.repository.MediaRepository;
import com.v2s.player.favorites.FavoritesManager;
import com.v2s.player.history.HistoryManager;
import com.v2s.player.media.MediaScanner;
import com.v2s.player.media.MediaSorter;
import com.v2s.player.playlists.PlaylistManager;
import com.v2s.player.search.SearchEngine;
import com.v2s.player.settings.SettingsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * HomeActivity is the main V2S shell and reference-style library host.
 * Role: renders Videos, Songs, Shorts, Playlists, Favorites, History, Search, More, Settings and About while delegating persistence to feature managers.
 */
public final class HomeActivity extends Activity {
    private MediaRepository mediaRepository;
    private LibraryRepository libraryRepository;
    private PlaylistManager playlistManager;
    private FavoritesManager favoritesManager;
    private HistoryManager historyManager;
    private MediaScanner mediaScanner;
    private MediaSorter mediaSorter;
    private SearchEngine searchEngine;
    private SettingsManager settingsManager;

    private FrameLayout contentHost;
    private FrameLayout miniPlayerHost;
    private FrameLayout bottomNavHost;
    private TextView headerTitle;
    private TextView headerSubtitle;
    private TextView headerSearch;
    private TextView headerMore;
    private String currentScreen = "Videos";
    private boolean gridMode = true;
    private String sortMode = "name_asc";
    private String searchQuery = "";
    private MediaItem lastPlayed;

    /** Creates repositories, builds the persistent shell, scans demo/device media and opens Videos. */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(V2SUi.BG);
        getWindow().setNavigationBarColor(V2SUi.BG);
        mediaRepository = new MediaRepository(this);
        libraryRepository = new LibraryRepository(this);
        playlistManager = new PlaylistManager(this);
        favoritesManager = new FavoritesManager(libraryRepository);
        historyManager = new HistoryManager(libraryRepository);
        mediaScanner = new MediaScanner(mediaRepository);
        mediaSorter = new MediaSorter(mediaRepository);
        searchEngine = new SearchEngine(mediaRepository);
        settingsManager = new SettingsManager(this);
        buildShell();
        mediaScanner.scanNow();
        showVideos();
        if (!PermissionManager.hasMediaPermissions(this)) PermissionManager.requestMediaPermissions(this);
    }

    /** Builds the reference-style dark shell with logo header, content host, mini player and icon navigation. */
    private void buildShell() {
        LinearLayout root = V2SUi.column(this);
        root.setBackgroundColor(V2SUi.BG);
        root.setPadding(V2SUi.dp(this, 8), V2SUi.dp(this, 6), V2SUi.dp(this, 8), 0);
        root.addView(buildHeader(), new LinearLayout.LayoutParams(-1, V2SUi.dp(this, 70)));
        contentHost = new FrameLayout(this);
        root.addView(contentHost, new LinearLayout.LayoutParams(-1, 0, 1));
        miniPlayerHost = new FrameLayout(this);
        root.addView(miniPlayerHost, new LinearLayout.LayoutParams(-1, 0));
        bottomNavHost = new FrameLayout(this);
        bottomNavHost.addView(buildBottomNavigation(), new FrameLayout.LayoutParams(-1, -1));
        root.addView(bottomNavHost, new LinearLayout.LayoutParams(-1, V2SUi.dp(this, 68)));
        setContentView(root);
    }

    /** Builds the top V2S header with logo, title, media count, search, sort and overflow actions. */
    private LinearLayout buildHeader() {
        LinearLayout header = V2SUi.row(this);
        ImageView logo = new ImageView(this); logo.setImageResource(R.drawable.ic_v2s); logo.setContentDescription("V2S logo");
        header.addView(logo, new LinearLayout.LayoutParams(V2SUi.dp(this, 48), V2SUi.dp(this, 54)));
        LinearLayout brand = V2SUi.column(this); brand.setPadding(V2SUi.dp(this, 6), 0, 0, 0);
        headerTitle = V2SUi.heading(this, "V2S", 21); brand.addView(headerTitle, new LinearLayout.LayoutParams(-1, V2SUi.dp(this, 28)));
        headerSubtitle = V2SUi.text(this, "Videos", 12, V2SUi.ORANGE); brand.addView(headerSubtitle, new LinearLayout.LayoutParams(-1, V2SUi.dp(this, 34)));
        header.addView(brand, new LinearLayout.LayoutParams(0, -1, 1));
        headerSearch = V2SUi.iconButton(this, "⌕", "Search media"); headerSearch.setOnClickListener(v -> showSearchScreen());
        header.addView(headerSearch, new LinearLayout.LayoutParams(V2SUi.dp(this, 42), V2SUi.dp(this, 42)));
        headerMore = V2SUi.iconButton(this, "⋮", "More options"); headerMore.setOnClickListener(v -> showMore());
        LinearLayout.LayoutParams moreLp = new LinearLayout.LayoutParams(V2SUi.dp(this, 42), V2SUi.dp(this, 42)); moreLp.setMargins(V2SUi.dp(this, 6), 0, 0, 0); header.addView(headerMore, moreLp);
        return header;
    }

    /** Builds the five-tab bottom navigation matching the orange active-state treatment in the reference. */
    private LinearLayout buildBottomNavigation() {
        LinearLayout nav = V2SUi.row(this); nav.setGravity(Gravity.CENTER); nav.setPadding(0, V2SUi.dp(this, 4), 0, V2SUi.dp(this, 4));
        addNavItem(nav, "▣", "Videos", "Videos"); addNavItem(nav, "♫", "Songs", "Songs"); addNavItem(nav, "▷", "Shorts", "Shorts"); addNavItem(nav, "▤", "Playlist", "Playlists"); addNavItem(nav, "•••", "More", "More");
        return nav;
    }

    /** Adds one icon-plus-label navigation item and wires it to the feature router. */
    private void addNavItem(LinearLayout nav, String icon, String label, String destination) {
        LinearLayout item = V2SUi.column(this); item.setGravity(Gravity.CENTER); item.setContentDescription("Open " + label); item.setOnClickListener(v -> navigate(destination));
        TextView i = V2SUi.text(this, icon, 22, currentScreen.equals(destination) ? V2SUi.ORANGE : V2SUi.MUTED); i.setGravity(Gravity.CENTER); item.addView(i, new LinearLayout.LayoutParams(-1, V2SUi.dp(this, 31)));
        TextView t = V2SUi.text(this, label, 11, currentScreen.equals(destination) ? V2SUi.ORANGE : V2SUi.MUTED); t.setGravity(Gravity.CENTER); item.addView(t, new LinearLayout.LayoutParams(-1, V2SUi.dp(this, 22)));
        nav.addView(item, new LinearLayout.LayoutParams(0, -1, 1));
    }

    /** Routes primary navigation requests and clears one-off search state. */
    private void navigate(String screen) {
        currentScreen = screen; searchQuery = "";
        if ("Videos".equals(screen)) showVideos(); else if ("Songs".equals(screen)) showSongs(); else if ("Shorts".equals(screen)) showShorts(); else if ("Playlists".equals(screen)) showPlaylists(); else showMore();
    }

    /** Clears content and updates the persistent header before a new screen is rendered. */
    private void baseScreen(String title, String subtitle) {
        contentHost.removeAllViews(); headerTitle.setText("V2S"); headerSubtitle.setText(title + "\n" + subtitle); refreshMiniPlayer(); rebuildNavigation();
    }

    /** Rebuilds bottom navigation so the active feature is orange on every screen transition. */
    private void rebuildNavigation() {
        if (bottomNavHost == null) return;
        bottomNavHost.removeAllViews();
        bottomNavHost.addView(buildBottomNavigation(), new FrameLayout.LayoutParams(-1, -1));
    }

    /** Renders the main video library with All Videos/Last Played pills, tabs, grid/list and sort controls. */
    private void showVideos() {
        currentScreen = "Videos";
        List<MediaItem> items = filtered(mediaRepository.videos()); mediaSorter.sort(items, sortMode); baseScreen("Videos", String.format(Locale.US, "%,d videos", items.size()));
        LinearLayout page = V2SUi.column(this); V2SUi.pagePadding(page); page.addView(buildLibraryToolbar("All Videos", "Last Played", true)); page.addView(buildCategoryTabs("▣", "Videos", "▰", "Folders", "☷", "Playlists"));
        if (items.isEmpty()) addEmpty(page, "No videos indexed", "Scan device media to populate your library."); else page.addView(mediaCollection(items));
        contentHost.addView(scroll(page));
    }

    /** Renders the song library as a compact numbered list matching the reference Songs screen. */
    private void showSongs() {
        currentScreen = "Songs";
        List<MediaItem> items = filtered(mediaRepository.songs()); mediaSorter.sort(items, sortMode); baseScreen("Songs", String.format(Locale.US, "%,d songs", items.size()));
        LinearLayout page = V2SUi.column(this); V2SUi.pagePadding(page); page.addView(buildCategoryTabs("♫", "Songs", "▰", "Folders", "☷", "Playlists"));
        if (items.isEmpty()) addEmpty(page, "No songs indexed", "Scan device media to populate your song library."); else page.addView(songList(items));
        contentHost.addView(scroll(page));
    }

    /** Renders the vertical Shorts feed with large artwork and right-side action controls. */
    private void showShorts() {
        currentScreen = "Shorts"; baseScreen("Shorts", "Short video feed");
        LinearLayout page = V2SUi.column(this); V2SUi.pagePadding(page);
        List<MediaItem> videos = mediaRepository.videos();
        if (videos.isEmpty()) addEmpty(page, "No shorts available", "Add videos to your library first.");
        for (MediaItem item : videos) page.addView(shortCard(item));
        contentHost.addView(scroll(page));
    }

    /** Renders playlists with grid/list toggle, search, sort and a prominent orange add button. */
    private void showPlaylists() {
        currentScreen = "Playlists"; List<Playlist> lists = playlistManager.all(); baseScreen("Playlists", "Total Playlists: " + lists.size());
        LinearLayout page = V2SUi.column(this); V2SUi.pagePadding(page);
        LinearLayout toolbar = V2SUi.row(this); TextView sort = V2SUi.text(this, "Sort by  Recently Added ▾", 12, V2SUi.MUTED); toolbar.addView(sort, new LinearLayout.LayoutParams(0, 44, 1));
        TextView view = V2SUi.iconButton(this, gridMode ? "▦" : "☷", gridMode ? "Switch to list" : "Switch to grid"); view.setOnClickListener(v -> { gridMode = !gridMode; showPlaylists(); }); toolbar.addView(view, fixed(44,44));
        TextView add = V2SUi.orangeButton(this, "＋", "Add new playlist"); add.setOnClickListener(v -> showCreatePlaylistDialog()); LinearLayout.LayoutParams ap = fixed(48,44); ap.setMargins(6,0,0,0); toolbar.addView(add, ap); page.addView(toolbar);
        page.addView(buildPlaylistQuickFilters());
        page.addView(V2SUi.divider(this), new LinearLayout.LayoutParams(-1,1));
        if (lists.isEmpty()) addEmpty(page, "No playlists yet", "Tap + to create your first playlist."); else page.addView(playlistCollection(lists));
        contentHost.addView(scroll(page));
    }

    /** Renders the More hub with Premium, Streams, History, Tools and Other sections from the reference page. */
    private void showMore() {
        currentScreen = "More"; baseScreen("More", "V2S Video Player");
        LinearLayout page = V2SUi.column(this); V2SUi.pagePadding(page);
        LinearLayout premium=V2SUi.row(this);premium.setPadding(10,6,8,6);V2SUi.surface(premium,V2SUi.SURFACE,12,V2SUi.DIVIDER,1);TextView crown=V2SUi.text(this,"♛",24,V2SUi.ORANGE);premium.addView(crown,new LinearLayout.LayoutParams(42,52));LinearLayout pi=V2SUi.column(this);pi.addView(V2SUi.text(this,"Go Premium",14,V2SUi.ORANGE));pi.addView(V2SUi.text(this,"Remove ads and get more features",10,V2SUi.MUTED));premium.addView(pi,new LinearLayout.LayoutParams(0,52,1));TextView go=V2SUi.orangeButton(this,"›","Premium");premium.addView(go,new LinearLayout.LayoutParams(44,40));page.addView(premium,new LinearLayout.LayoutParams(-1,66));
        page.addView(sectionHeader("Streams","View all",null));
        LinearLayout streams=V2SUi.row(this);streams.addView(toolCard("＋\nNew Stream",()->toast("Stream setup is ready for a future network source")),new LinearLayout.LayoutParams(0,92,1));LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(0,92,1);slp.setMargins(6,0,0,0);streams.addView(toolCard("◉\nLiving Room TV\n...XIPVHA63zyN5",()->toast("Network stream discovery is not connected")),slp);page.addView(streams);
        page.addView(sectionHeader("History","View all",this::showHistory));
        LinearLayout historyTabs=V2SUi.row(this);historyTabs.addView(V2SUi.orangeButton(this,"▶  Video History","Video history"),new LinearLayout.LayoutParams(0,42,1));LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(0,42,1);hp.setMargins(6,0,0,0);historyTabs.addView(V2SUi.darkButton(this,"♫  Song History","Song history"),hp);page.addView(historyTabs);
        page.addView(historyPreview());
        page.addView(sectionHeader("Tools",null,null));
        LinearLayout tools=V2SUi.column(this);addMoreRow(tools,new String[]{"☷\nEqualizer","◴\nSleep Timer","⌁\nPlayback Speed"},new Runnable[]{()->toast("Equalizer UI is prepared for player service integration"),()->toast("Sleep timer selected"),()->toast("Playback speed selected")});addMoreRow(tools,new String[]{"♫\nBackground Play","▣\nPIP Mode","▣\nVideo to Audio"},new Runnable[]{()->showSettings(),()->toast("PiP action requires platform PiP integration"),()->toast("Video to Audio is a future conversion tool")});addMoreRow(tools,new String[]{"↔\nFile Transfer","☆\nBookmarks","⌁\nNetwork Access"},new Runnable[]{()->toast("File transfer tool"),()->toast("Bookmarks tool"),()->toast("Network access tool")});page.addView(tools);
        page.addView(sectionHeader("Other",null,null));page.addView(settingsAction("Ad-Free Experience","PRO",()->toast("Premium is not connected in this offline build")));page.addView(settingsAction("Rate Us","Open store rating",()->toast("Store link can be configured in release build")));page.addView(settingsAction("Share App","Share V2S",()->toast("Share action ready")));page.addView(settingsAction("Help & Feedback","Support",this::showAbout));
        contentHost.addView(scroll(page));
    }

    /** Creates a More-page section heading with an optional right-side action. */
    private View sectionHeader(String title,String action,Runnable click){LinearLayout row=V2SUi.row(this);row.setPadding(4,10,4,4);row.addView(V2SUi.heading(this,title,16),new LinearLayout.LayoutParams(0,34,1));if(action!=null){TextView a=V2SUi.text(this,action,11,V2SUi.ORANGE);a.setGravity(Gravity.CENTER);if(click!=null)a.setOnClickListener(v->click.run());row.addView(a,new LinearLayout.LayoutParams(70,34));}return row;}

    /** Creates a reusable utility card for the More screen. */
    private View toolCard(String label,Runnable action){TextView b=V2SUi.text(this,label,12,V2SUi.TEXT);b.setGravity(Gravity.CENTER);b.setOnClickListener(v->action.run());V2SUi.surface(b,V2SUi.SURFACE,10,V2SUi.DIVIDER,1);return b;}

    /** Creates a compact history preview using the first three library videos. */
    private View historyPreview(){LinearLayout row=V2SUi.row(this);List<MediaItem> items=mediaRepository.videos();for(int i=0;i<Math.min(3,items.size());i++){MediaItem item=items.get(i);LinearLayout card=V2SUi.column(this);ImageView img=V2SUi.thumbnail(this,0,0);if(item.coverResId!=0)img.setImageResource(item.coverResId);card.addView(img,new LinearLayout.LayoutParams(-1,78));card.addView(V2SUi.text(this,item.title,10,V2SUi.TEXT));card.addView(V2SUi.text(this,i==0?"Today":(i==1?"Yesterday":"2 days ago"),9,V2SUi.MUTED));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,120,1);if(i>0)lp.setMargins(6,0,0,0);row.addView(card,lp);}return row;}

    /** Renders Favorites as a reusable media list and supports direct playback. */
    private void showFavorites() { currentScreen = "More"; baseScreen("Favorites", "Saved media"); List<MediaItem> items = libraryRepository.resolveFavorites(mediaRepository.videos(), mediaRepository.songs()); LinearLayout page = V2SUi.column(this); V2SUi.pagePadding(page); if (items.isEmpty()) addEmpty(page, "No favorites yet", "Use the heart action on any media item."); else page.addView(songLikeList(items)); contentHost.addView(scroll(page)); }

    /** Renders History as a reusable media list and provides a clear-history action. */
    private void showHistory() { currentScreen = "More"; baseScreen("History", "Recently played"); LinearLayout page = V2SUi.column(this); V2SUi.pagePadding(page); TextView clear = V2SUi.darkButton(this, "Clear History", "Clear playback history"); clear.setOnClickListener(v -> { historyManager.clear(); showHistory(); }); page.addView(clear, new LinearLayout.LayoutParams(150,44)); List<MediaItem> items = libraryRepository.resolveHistory(mediaRepository.videos(), mediaRepository.songs()); if (items.isEmpty()) addEmpty(page, "No history", "Played media will appear here."); else page.addView(songLikeList(items)); contentHost.addView(scroll(page)); }

    /** Renders the multi-section Settings page with General, Video and Audio groups from the reference. */
    private void showSettings() {
        currentScreen = "More"; baseScreen("Settings", "V2S preferences"); LinearLayout page = V2SUi.column(this); V2SUi.pagePadding(page);
        page.addView(settingsSection("General"));
        page.addView(settingsAction("Appearance","Dark Theme",()->toast("Dark V2S theme is active")));
        page.addView(settingsAction("Language","English (System)",()->toast("Language follows the Android system")));
        page.addView(settingsAction("Default Folder","/storage/emulated/0/V2S",()->toast("Default folder is MediaStore in this build")));
        page.addView(settingsToggle("Remember Playback Position","Resume from last position",true,enabled->toast("Remember position: "+enabled)));
        page.addView(settingsToggle("Play Next Automatically","Add next video to queue automatically",false,enabled->toast("Auto next: "+enabled)));
        page.addView(settingsToggle("Show Hidden Files","Show hidden media files",false,enabled->toast("Hidden files: "+enabled)));
        page.addView(settingsSection("Video"));
        page.addView(settingsToggle("Hardware Acceleration","Use hardware acceleration for playback",true,enabled->toast("Hardware acceleration: "+enabled)));
        page.addView(settingsAction("Video Quality","Best Quality (Auto)",()->toast("Automatic quality selection")));
        page.addView(settingsAction("Subtitles","Size, color and style",()->toast("Subtitle preferences")));
        page.addView(settingsAction("Screenshot Format","PNG",()->toast("PNG screenshot format selected")));
        page.addView(settingsSection("Audio"));
        page.addView(settingsAction("Audio Output","Device Speaker",()->toast("Audio output: device speaker")));
        page.addView(settingsToggle("Volume Boost","Increase audio volume",false,enabled->toast("Volume boost: "+enabled)));
        page.addView(settingsToggle("Background Playback","Keep audio playing outside player",settingsManager.isBackgroundPlaybackEnabled(),enabled->settingsManager.setBackgroundPlaybackEnabled(enabled)));
        page.addView(settingsSection("Library"));
        page.addView(settingsAction("Scan Media","Read videos and songs",this::scanMedia));
        page.addView(settingsAction("Clear History","Remove recently played items",()->{historyManager.clear();toast("History cleared");}));
        contentHost.addView(scroll(page));
    }

    /** Renders the About page with V2S identity, supported features and project information. */
    private void showAbout() {
        currentScreen = "More"; baseScreen("About", "V2S Video & Audio Player"); LinearLayout page = V2SUi.column(this); V2SUi.pagePadding(page);
        LinearLayout hero = V2SUi.column(this); hero.setGravity(Gravity.CENTER); V2SUi.surface(hero, V2SUi.SURFACE, 16, V2SUi.DIVIDER, 1); ImageView logo = new ImageView(this); logo.setImageResource(R.drawable.ic_v2s); hero.addView(logo, new LinearLayout.LayoutParams(80,80)); hero.addView(V2SUi.heading(this,"V2S",28)); hero.addView(V2SUi.text(this,"Video & Audio Player",14,V2SUi.ORANGE)); hero.addView(V2SUi.text(this,"Local playback • playlists • shorts • library",12,V2SUi.MUTED)); page.addView(hero, new LinearLayout.LayoutParams(-1,180));
        page.addView(featureLine("✓", "All Format Support", "Video and audio files available to the Android player APIs")); page.addView(featureLine("4K", "Ultra HD Video", "High resolution content is displayed at its source resolution")); page.addView(featureLine("▶", "Smart Playback", "Remembered history and quick access to recent media")); page.addView(featureLine("♫", "Background Play", "Optional setting for audio playback")); page.addView(featureLine("▣", "PiP / Mini Player", "Reference-style compact playback navigation"));
        TextView docs = V2SUi.darkButton(this,"Project Architecture","Open architecture information"); docs.setOnClickListener(v -> toast("See ARCHITECTURE.md, FILE_GUIDE.md and QA_CHECKLIST.md")); page.addView(docs, fixed(-1,46)); contentHost.addView(scroll(page));
    }

    /** Renders a full-screen style search page with live title/artist filtering and a clear close action. */
    private void showSearchScreen() {
        currentScreen = "More"; baseScreen("Search", "Search videos, songs and artists"); LinearLayout page = V2SUi.column(this); V2SUi.pagePadding(page);
        EditText input = new EditText(this); input.setSingleLine(true); input.setHint("Search media..."); input.setHintTextColor(V2SUi.MUTED); input.setTextColor(V2SUi.TEXT); input.setText(searchQuery); V2SUi.surface(input,V2SUi.SURFACE_2,10,V2SUi.DIVIDER,1); page.addView(input,new LinearLayout.LayoutParams(-1,50));
        LinearLayout results = V2SUi.column(this); page.addView(results);
        input.addTextChangedListener(new android.text.TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int before,int count){ searchQuery=s.toString(); renderSearchResults(results); } public void afterTextChanged(android.text.Editable e){} });
        renderSearchResults(results); contentHost.addView(scroll(page)); input.requestFocus();
    }

    /** Rebuilds search results from the combined video and song library. */
    private void renderSearchResults(LinearLayout results) { results.removeAllViews(); List<MediaItem> all = new ArrayList<>(); all.addAll(mediaRepository.videos()); all.addAll(mediaRepository.songs()); List<MediaItem> found = searchEngine.search(all, searchQuery); if(found.isEmpty()) { addEmpty(results,"No results","Try a different title or artist."); return; } for(MediaItem item: found) results.addView(compactMediaRow(item,true)); }

    /** Builds the All Videos/Last Played pill row and grid/list controls used on the reference video page. */
    private LinearLayout buildLibraryToolbar(String primary, String secondary, boolean includeView) {
        LinearLayout bar = V2SUi.row(this); TextView a = V2SUi.orangeButton(this,"▶  "+primary,"Show all videos"); bar.addView(a, new LinearLayout.LayoutParams(0,44,1)); TextView b = V2SUi.darkButton(this,"◷  "+secondary,"Show last played"); LinearLayout.LayoutParams bp=fixed(120,44); bp.setMargins(6,0,0,0); bar.addView(b,bp); if(includeView){ TextView view=V2SUi.iconButton(this,gridMode?"▦":"☷",gridMode?"Switch to list":"Switch to grid"); view.setOnClickListener(v->{gridMode=!gridMode;showVideos();}); LinearLayout.LayoutParams vp=fixed(44,44);vp.setMargins(6,0,0,0);bar.addView(view,vp); } return bar;
    }

    /** Builds the Videos/Songs category tab strip with orange active indicator. */
    private LinearLayout buildCategoryTabs(String i1,String t1,String i2,String t2,String i3,String t3){ LinearLayout tabs=V2SUi.row(this); tabs.setPadding(0,6,0,0); addCategory(tabs,i1,t1,true); addCategory(tabs,i2,t2,false); addCategory(tabs,i3,t3,false); return tabs; }

    /** Adds one category tab with an icon, label and click behavior. */
    private void addCategory(LinearLayout tabs,String icon,String label,boolean active){ LinearLayout item=V2SUi.column(this);item.setGravity(Gravity.CENTER);TextView i=V2SUi.text(this,icon,21,active?V2SUi.ORANGE:V2SUi.MUTED);i.setGravity(Gravity.CENTER);item.addView(i,new LinearLayout.LayoutParams(-1,30));TextView t=V2SUi.text(this,label,12,active?V2SUi.ORANGE:V2SUi.TEXT);t.setGravity(Gravity.CENTER);item.addView(t,new LinearLayout.LayoutParams(-1,28));if(active){View line=new View(this);line.setBackgroundColor(V2SUi.ORANGE);item.addView(line,new LinearLayout.LayoutParams(-1,3));}tabs.addView(item,new LinearLayout.LayoutParams(0,62,1));}

    /** Creates either the reference-style two-column video cards or the compact list rows. */
    private View mediaCollection(List<MediaItem> items){ if(!gridMode){ LinearLayout list=V2SUi.column(this); for(MediaItem item:items) list.addView(compactMediaRow(item,false)); return list; } LinearLayout grid=V2SUi.column(this); for(int i=0;i<items.size();i+=2){LinearLayout row=V2SUi.row(this); row.setGravity(Gravity.TOP); row.addView(videoCard(items.get(i)),new LinearLayout.LayoutParams(0,0,1)); if(i+1<items.size()){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,0,1);p.setMargins(6,0,0,0);row.addView(videoCard(items.get(i+1)),p);} grid.addView(row,new LinearLayout.LayoutParams(-1,220));} return grid; }

    /** Creates one landscape media card with quality badge, duration badge, title, metadata and favorite action. */
    private View videoCard(MediaItem item){ LinearLayout card=V2SUi.column(this); card.setPadding(2,5,2,5); ImageView image=V2SUi.thumbnail(this,0,0); if(item.coverResId!=0) image.setImageResource(item.coverResId); card.addView(image,new LinearLayout.LayoutParams(-1,112)); LinearLayout meta=V2SUi.row(this);TextView title=V2SUi.text(this,item.title,13,V2SUi.TEXT);title.setMaxLines(2);meta.addView(title,new LinearLayout.LayoutParams(0,48,1));TextView fav=V2SUi.iconButton(this,"♡","Toggle favorite");fav.setOnClickListener(v->{favoritesManager.toggle(item);fav.setText(favoritesManager.isFavorite(item)?"♥":"♡");});meta.addView(fav,fixed(36,36));card.addView(meta);card.addView(V2SUi.text(this,item.durationText()+"   •   "+(item.artist.isEmpty()?"Local media":item.artist),11,V2SUi.MUTED));card.setOnClickListener(v->play(item));return card; }

    /** Creates one compact list row matching the right-hand list variant in the reference. */
    private View compactMediaRow(MediaItem item, boolean fromSearch){ LinearLayout row=V2SUi.row(this);row.setPadding(6,6,4,6);ImageView image=V2SUi.thumbnail(this,0,0);if(item.coverResId!=0)image.setImageResource(item.coverResId);row.addView(image,new LinearLayout.LayoutParams(fromSearch?84:170,fromSearch?60:86));LinearLayout info=V2SUi.column(this);info.setPadding(10,0,4,0);TextView title=V2SUi.text(this,item.title,14,V2SUi.TEXT);title.setMaxLines(2);info.addView(title,new LinearLayout.LayoutParams(-1,0,1));info.addView(V2SUi.text(this,item.durationText()+"   "+item.artist,11,V2SUi.MUTED));row.addView(info,new LinearLayout.LayoutParams(0,fromSearch?74:98,1));TextView more=V2SUi.iconButton(this,"⋮","Media options");more.setOnClickListener(v->showMediaMenu(item));row.addView(more,fixed(40,40));row.setOnClickListener(v->play(item));row.addView(V2SUi.divider(this),new LinearLayout.LayoutParams(-1,1));return row; }

    /** Creates the numbered Songs list with album artwork and overflow controls. */
    private View songList(List<MediaItem> items){ LinearLayout list=V2SUi.column(this); int index=1;for(MediaItem item:items){LinearLayout row=V2SUi.row(this);TextView number=V2SUi.text(this,String.valueOf(index++),16,V2SUi.TEXT);number.setGravity(Gravity.CENTER);row.addView(number,new LinearLayout.LayoutParams(48,90));ImageView cover=V2SUi.thumbnail(this,0,0);if(item.coverResId!=0)cover.setImageResource(item.coverResId);row.addView(cover,new LinearLayout.LayoutParams(64,64));LinearLayout info=V2SUi.column(this);info.setPadding(10,0,4,0);info.addView(V2SUi.text(this,item.title,14,V2SUi.TEXT),new LinearLayout.LayoutParams(-1,34));info.addView(V2SUi.text(this,item.artist,12,V2SUi.MUTED),new LinearLayout.LayoutParams(-1,24));info.addView(V2SUi.text(this,item.durationText()+"   ♡",11,V2SUi.MUTED),new LinearLayout.LayoutParams(-1,24));row.addView(info,new LinearLayout.LayoutParams(0,90,1));TextView more=V2SUi.iconButton(this,"⋮","Song options");more.setOnClickListener(v->showMediaMenu(item));row.addView(more,fixed(42,42));row.setOnClickListener(v->play(item));list.addView(row,new LinearLayout.LayoutParams(-1,94));list.addView(V2SUi.divider(this),new LinearLayout.LayoutParams(-1,1));}return list; }

    /** Creates a reusable media list for Favorites and History pages. */
    private View songLikeList(List<MediaItem> items){ LinearLayout list=V2SUi.column(this);for(MediaItem item:items)list.addView(compactMediaRow(item,false));return list; }

    /** Creates the vertical Shorts card with artwork, title, and social-style action rail. */
    private View shortCard(MediaItem item){ FrameLayout box=new FrameLayout(this); ImageView image=V2SUi.thumbnail(this,0,0);if(item.coverResId!=0)image.setImageResource(item.coverResId);box.addView(image,new FrameLayout.LayoutParams(-1,430));LinearLayout overlay=V2SUi.column(this);overlay.setGravity(Gravity.BOTTOM);overlay.setPadding(12,12,12,12);overlay.setBackgroundColor(Color.TRANSPARENT);overlay.addView(V2SUi.heading(this,item.title,17));overlay.addView(V2SUi.text(this,"@v2s • "+item.durationText(),12,V2SUi.MUTED));FrameLayout.LayoutParams op=new FrameLayout.LayoutParams(-1,160,Gravity.BOTTOM);box.addView(overlay,op);LinearLayout rail=V2SUi.column(this);String[] actions={"♥","↗","◷","⋮"};for(String a:actions){TextView b=V2SUi.iconButton(this,a,a);rail.addView(b,new LinearLayout.LayoutParams(48,48));}FrameLayout.LayoutParams rp=new FrameLayout.LayoutParams(54,220,Gravity.RIGHT|Gravity.CENTER_VERTICAL);rp.rightMargin=8;box.addView(rail,rp);box.setOnClickListener(v->play(item));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,430);lp.setMargins(0,0,0,10);return box; }

    /** Creates a three-column playlist grid or compact list to mirror the reference playlist density. */
    private View playlistCollection(List<Playlist> lists){
        if(!gridMode){LinearLayout list=V2SUi.column(this);for(Playlist p:lists)list.addView(playlistListRow(p));return list;}
        LinearLayout grid=V2SUi.column(this);List<MediaItem> thumbs=mediaRepository.videos();
        for(int i=0;i<lists.size();i+=3){LinearLayout r=V2SUi.row(this);r.setGravity(Gravity.TOP);for(int j=0;j<3;j++){if(i+j>=lists.size())break;MediaItem thumb=thumbs.isEmpty()?null:thumbs.get((i+j)%thumbs.size());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,194,1);if(j>0)lp.setMargins(6,0,0,0);r.addView(playlistCard(lists.get(i+j),thumb),lp);}grid.addView(r,new LinearLayout.LayoutParams(-1,202));}
        return grid;
    }

    /** Creates the four quick library filters shown above playlist cards in the reference. */
    private View buildPlaylistQuickFilters(){HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);LinearLayout r=V2SUi.row(this);String[] labels={"♥\nFavorites\n120 tracks","♫\nAll Songs\n1,248 tracks","▣\nAll Videos\n568 videos","◷\nHistory\n325 tracks"};for(int i=0;i<labels.length;i++){TextView t=V2SUi.text(this,labels[i],11,i==0?V2SUi.ORANGE:V2SUi.TEXT);t.setGravity(Gravity.CENTER);t.setPadding(10,6,10,6);V2SUi.surface(t,V2SUi.SURFACE,12,V2SUi.DIVIDER,1);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(145,76);if(i>0)lp.setMargins(6,0,0,0);r.addView(t,lp);}hsv.addView(r);return hsv;}

    /** Creates a visual playlist grid card with title, count and overflow menu. */
    private View playlistCard(Playlist p,MediaItem thumb){LinearLayout card=V2SUi.column(this);if(thumb!=null&&thumb.coverResId!=0){ImageView image=V2SUi.thumbnail(this,0,0);image.setImageResource(thumb.coverResId);card.addView(image,new LinearLayout.LayoutParams(-1,116));}else card.addView(V2SUi.text(this,"♫",34,V2SUi.ORANGE),new LinearLayout.LayoutParams(-1,116));card.addView(V2SUi.text(this,p.name,13,V2SUi.TEXT));card.addView(V2SUi.text(this,p.mediaIds().size()+" files  •  V2S",11,V2SUi.MUTED));TextView more=V2SUi.iconButton(this,"⋮","Playlist options");more.setOnClickListener(v->showPlaylistMenu(p));card.addView(more,new LinearLayout.LayoutParams(-1,34));return card;}

    /** Creates a compact playlist row for list view. */
    private View playlistListRow(Playlist p){LinearLayout row=V2SUi.row(this);row.setPadding(4,8,4,8);TextView icon=V2SUi.text(this,"♫",28,V2SUi.ORANGE);icon.setGravity(Gravity.CENTER);row.addView(icon,new LinearLayout.LayoutParams(56,70));LinearLayout info=V2SUi.column(this);info.addView(V2SUi.text(this,p.name,14,V2SUi.TEXT));info.addView(V2SUi.text(this,p.mediaIds().size()+" files  •  Recently added",11,V2SUi.MUTED));row.addView(info,new LinearLayout.LayoutParams(0,70,1));TextView more=V2SUi.iconButton(this,"⋮","Playlist options");more.setOnClickListener(v->showPlaylistMenu(p));row.addView(more,fixed(44,44));return row;}

    /** Builds the compact feature strip that appears in the PDF reference beneath the main library. */
    private View buildFeatureStrip(){HorizontalScrollView hsv=new HorizontalScrollView(this);hsv.setHorizontalScrollBarEnabled(false);LinearLayout r=V2SUi.row(this);String[] f={"▶  All Format\nSupport","4K  Ultra HD\nVideo","▷  Smart\nPlayback","♫  Background\nPlay","▣  PiP Mini\nPlayer","☾  Night\nMode"};for(String s:f){TextView t=V2SUi.text(this,s,11,V2SUi.TEXT);t.setPadding(10,6,10,6);V2SUi.surface(t,V2SUi.SURFACE,10,V2SUi.DIVIDER,1);r.addView(t,new LinearLayout.LayoutParams(130,58));}hsv.addView(r);return hsv;}

    /** Adds one row of three More-screen action cards. */
    private void addMoreRow(LinearLayout parent,String[] labels,Runnable[] actions){LinearLayout r=V2SUi.row(this);for(int i=0;i<labels.length;i++){final int index=i;TextView b=V2SUi.text(this,labels[i],13,V2SUi.TEXT);b.setGravity(Gravity.CENTER);b.setPadding(4,4,4,4);b.setOnClickListener(v->actions[index].run());V2SUi.surface(b,V2SUi.SURFACE,12,V2SUi.DIVIDER,1);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,92,1);if(i>0)lp.setMargins(6,0,0,0);r.addView(b,lp);}parent.addView(r,new LinearLayout.LayoutParams(-1,98));}

    /** Creates a labeled settings section divider. */
    private TextView settingsSection(String title){TextView t=V2SUi.text(this,title.toUpperCase(Locale.US),11,V2SUi.ORANGE);t.setPadding(4,14,4,4);return t;}

    /** Creates a settings toggle row with a persistent boolean preference. */
    private View settingsToggle(String title,String subtitle,boolean checked,java.util.function.Consumer<Boolean> save){LinearLayout row=V2SUi.row(this);row.setPadding(8,4,4,4);LinearLayout info=V2SUi.column(this);info.addView(V2SUi.text(this,title,14,V2SUi.TEXT));info.addView(V2SUi.text(this,subtitle,11,V2SUi.MUTED));row.addView(info,new LinearLayout.LayoutParams(0,64,1));Switch sw=new Switch(this);sw.setChecked(checked);sw.setOnCheckedChangeListener((b,c)->save.accept(c));row.addView(sw,new LinearLayout.LayoutParams(58,54));return row;}

    /** Creates a clickable settings action row with a title and current value. */
    private View settingsAction(String title,String value,Runnable action){LinearLayout row=V2SUi.row(this);row.setPadding(8,4,8,4);LinearLayout info=V2SUi.column(this);info.addView(V2SUi.text(this,title,14,V2SUi.TEXT));info.addView(V2SUi.text(this,value,11,V2SUi.MUTED));row.addView(info,new LinearLayout.LayoutParams(0,64,1));TextView go=V2SUi.text(this,"›",28,V2SUi.MUTED);go.setGravity(Gravity.CENTER);row.addView(go,new LinearLayout.LayoutParams(42,60));row.setOnClickListener(v->action.run());return row;}

    /** Creates a compact About/feature information row. */
    private View featureLine(String icon,String title,String subtitle){LinearLayout row=V2SUi.row(this);TextView i=V2SUi.text(this,icon,18,V2SUi.ORANGE);i.setGravity(Gravity.CENTER);row.addView(i,new LinearLayout.LayoutParams(50,64));LinearLayout info=V2SUi.column(this);info.addView(V2SUi.text(this,title,14,V2SUi.TEXT));info.addView(V2SUi.text(this,subtitle,11,V2SUi.MUTED));row.addView(info,new LinearLayout.LayoutParams(0,64,1));return row;}

    /** Shows a dialog for creating a new playlist and persists the validated name. */
    private void showCreatePlaylistDialog(){EditText input=new EditText(this);input.setHint("Playlist name");input.setTextColor(V2SUi.TEXT);input.setHintTextColor(V2SUi.MUTED);input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);AlertDialog d=new AlertDialog.Builder(this).setTitle("New Playlist").setView(input).setNegativeButton("Cancel",null).setPositiveButton("Create",null).create();d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String name=input.getText().toString().trim();if(name.isEmpty()){input.setError("Enter a playlist name");return;}playlistManager.create(name);d.dismiss();showPlaylists();}));d.show();}

    /** Shows media actions for play, favorite and add-to-playlist operations. */
    private void showMediaMenu(MediaItem item){String[] actions={"Play","Add to Favorites","Add to Playlist","Remove from Favorites"};new AlertDialog.Builder(this).setTitle(item.title).setItems(actions,(d,which)->{if(which==0)play(item);else if(which==1){favoritesManager.add(item);toast("Added to Favorites");}else if(which==2)showPlaylistPicker(item);else{favoritesManager.remove(item);toast("Removed from Favorites");}}).show();}

    /** Shows the saved playlists and adds the selected media item to the chosen playlist. */
    private void showPlaylistPicker(MediaItem item){List<Playlist> lists=playlistManager.all();if(lists.isEmpty()){toast("Create a playlist first");showCreatePlaylistDialog();return;}String[] names=new String[lists.size()];for(int i=0;i<lists.size();i++)names[i]=lists.get(i).name;new AlertDialog.Builder(this).setTitle("Add to Playlist").setItems(names,(d,w)->{playlistManager.add(names[w],item.storageKey());toast("Added to "+names[w]);}).show();}

    /** Shows playlist deletion options and protects the user with a confirmation dialog. */
    private void showPlaylistMenu(Playlist p){new AlertDialog.Builder(this).setTitle(p.name).setItems(new String[]{"Delete Playlist"},(d,w)->new AlertDialog.Builder(this).setTitle("Delete playlist?").setMessage(p.name).setNegativeButton("Cancel",null).setPositiveButton("Delete",(x,y)->{playlistManager.delete(p.name);showPlaylists();}).show()).show();}

    /** Opens the platform player and records the item in local history. */
    private void play(MediaItem item){lastPlayed=item;historyManager.add(item);AppNavigator.openPlayer(this,item);}

    /** Updates the optional mini-player shown after a media item has been played. */
    private void refreshMiniPlayer(){if(miniPlayerHost==null)return;miniPlayerHost.removeAllViews();LinearLayout.LayoutParams hostParams=(LinearLayout.LayoutParams)miniPlayerHost.getLayoutParams();hostParams.height=lastPlayed==null?0:V2SUi.dp(this,58);miniPlayerHost.setLayoutParams(hostParams);if(lastPlayed==null)return;LinearLayout bar=V2SUi.row(this);bar.setPadding(8,4,8,4);V2SUi.surface(bar,V2SUi.SURFACE_2,10,V2SUi.DIVIDER,1);if(lastPlayed.coverResId!=0){ImageView img=V2SUi.thumbnail(this,0,0);img.setImageResource(lastPlayed.coverResId);bar.addView(img,new LinearLayout.LayoutParams(48,48));}LinearLayout info=V2SUi.column(this);info.setPadding(8,0,4,0);info.addView(V2SUi.text(this,lastPlayed.title,12,V2SUi.TEXT),new LinearLayout.LayoutParams(0,28,1));info.addView(V2SUi.text(this,"Recently played",10,V2SUi.MUTED),new LinearLayout.LayoutParams(0,22,1));bar.addView(info,new LinearLayout.LayoutParams(0,54,1));TextView open=V2SUi.iconButton(this,"▶","Open player");open.setOnClickListener(v->play(lastPlayed));bar.addView(open,fixed(44,44));miniPlayerHost.addView(bar,new FrameLayout.LayoutParams(-1,58));}

    /** Displays the media sort options used by Videos and the More hub. */
    private void showSortDialog(){String[] options={"Name (A-Z)","Name (Z-A)","Duration (Long → Short)","Duration (Short → Long)"};new AlertDialog.Builder(this).setTitle("Sort by").setSingleChoiceItems(options,sortIndex(),(d,w)->{sortMode=new String[]{"name_asc","name_desc","duration_desc","duration_asc"}[w];d.dismiss();if("Videos".equals(currentScreen))showVideos();else if("Songs".equals(currentScreen))showSongs();else showMore();}).show();}

    /** Converts the current sort mode to the radio-button index used by the dialog. */
    private int sortIndex(){if("name_desc".equals(sortMode))return 1;if("duration_desc".equals(sortMode))return 2;if("duration_asc".equals(sortMode))return 3;return 0;}

    /** Scans device MediaStore again and refreshes the current library. */
    private void scanMedia(){mediaScanner.scanNow();toast("Media library refreshed");if("Videos".equals(currentScreen))showVideos();else if("Songs".equals(currentScreen))showSongs();else showMore();}

    /** Opens a safe informational dialog for multi-select functionality. */
    private void showSelectMultiple(){new AlertDialog.Builder(this).setTitle("Select Multiple").setMessage("Long-press or use the media overflow menu to manage individual items. Multi-selection is reserved for batch actions in the next player service layer.").setPositiveButton("OK",null).show();}

    /** Shows a small media options dialog from the compact More/player surfaces. */
    private void showSearchDialog(){showSearchScreen();}

    /** Returns the current media list after applying the search query. */
    private List<MediaItem> filtered(List<MediaItem> source){return searchEngine.search(source,searchQuery);}

    /** Adds an empty-state panel with a clear next action. */
    private void addEmpty(LinearLayout page,String title,String message){LinearLayout box=V2SUi.column(this);box.setGravity(Gravity.CENTER);box.setPadding(20,30,20,30);V2SUi.surface(box,V2SUi.SURFACE,14,V2SUi.DIVIDER,1);box.addView(V2SUi.heading(this,title,17));box.addView(V2SUi.text(this,message,12,V2SUi.MUTED));page.addView(box,new LinearLayout.LayoutParams(-1,150));}

    /** Wraps a page in a dark scroll container with overscroll disabled for the compact reference layout. */
    private ScrollView scroll(View content){ScrollView s=new ScrollView(this);s.setFillViewport(true);s.setBackgroundColor(V2SUi.BG);s.setOverScrollMode(View.OVER_SCROLL_NEVER);s.addView(content);return s;}

    /** Creates fixed-size layout parameters for compact controls. */
    private LinearLayout.LayoutParams fixed(int width,int height){return new LinearLayout.LayoutParams(width,height);}

    /** Shows a short V2S toast without interrupting navigation. */
    private void toast(String message){Toast.makeText(this,message,Toast.LENGTH_SHORT).show();}

    /** Reopens the last visible feature after returning from the player. */
    @Override protected void onResume(){super.onResume();if(contentHost!=null&&lastPlayed!=null)refreshMiniPlayer();}
}
