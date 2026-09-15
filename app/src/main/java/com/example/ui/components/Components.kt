package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Post
import com.example.data.model.User
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryPurple

val BrandGradient = Brush.linearGradient(
    colors = listOf(PrimaryIndigo, PrimaryPurple, AccentCyan)
)

val StoryRingGradient = Brush.sweepGradient(
    colors = listOf(PrimaryIndigo, AccentPink, PrimaryPurple, AccentCyan, PrimaryIndigo)
)

@Composable
fun UserAvatar(
    avatarUrl: String?,
    displayName: String,
    size: Dp = 48.dp,
    isOnline: Boolean = false,
    hasStoryRing: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else modifier

    Box(
        modifier = clickableModifier.size(size + if (hasStoryRing) 8.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        // Optional story gradient ring
        if (hasStoryRing) {
            Box(
                modifier = Modifier
                    .size(size + 8.dp)
                    .clip(CircleShape)
                    .background(StoryRingGradient)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }

        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = displayName,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                val initial = displayName.firstOrNull()?.uppercase() ?: "?"
                Text(
                    text = initial,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.42f).sp
                )
            }
        }

        // Online green dot
        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(OnlineGreen)
            )
        }
    }
}

@Composable
fun FriendChatTopBar(
    title: String = "Friend Chat",
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    unreadNotificationsCount: Int = 0,
    onNotificationsClick: () -> Unit = {},
    onSearchClick: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                if (title == "Friend Chat") {
                    Text(
                        text = "Friend Chat",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp,
                        style = MaterialTheme.typography.titleLarge.copy(
                            brush = BrandGradient
                        )
                    )
                } else {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onSearchClick != null) {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("search_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("notifications_icon_button")
                ) {
                    BadgedBox(badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge(containerColor = AccentPink) {
                                Text(unreadNotificationsCount.toString(), color = Color.White)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (unreadNotificationsCount > 0) Icons.Default.Notifications else Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                actions?.invoke()
            }
        }
    }
}

@Composable
fun FriendChatBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    unreadChatsCount: Int = 0
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = {
                Icon(
                    imageVector = if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryIndigo,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
            selected = currentRoute == "discover",
            onClick = { onNavigate("discover") },
            icon = {
                Icon(
                    imageVector = if (currentRoute == "discover") Icons.Filled.Explore else Icons.Outlined.Explore,
                    contentDescription = "Discover"
                )
            },
            label = { Text("Discover", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryIndigo,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_discover")
        )

        NavigationBarItem(
            selected = currentRoute == "chats",
            onClick = { onNavigate("chats") },
            icon = {
                BadgedBox(badge = {
                    if (unreadChatsCount > 0) {
                        Badge(containerColor = PrimaryPurple) {
                            Text(unreadChatsCount.toString(), color = Color.White)
                        }
                    }
                }) {
                    Icon(
                        imageVector = if (currentRoute == "chats") Icons.Filled.ChatBubble else Icons.Filled.ChatBubbleOutline,
                        contentDescription = "Chats"
                    )
                }
            },
            label = { Text("Chats", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryIndigo,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_chats")
        )

        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { onNavigate("profile") },
            icon = {
                Icon(
                    imageVector = if (currentRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryIndigo,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.testTag("nav_profile")
        )
    }
}

@Composable
fun SocialPostCard(
    post: Post,
    onLikeClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Post Author Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAuthorClick() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    avatarUrl = post.authorAvatar,
                    displayName = post.authorName,
                    size = 42.dp,
                    hasStoryRing = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "@${post.authorUsername}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Post Image
            if (post.mediaUrl.isNotBlank()) {
                AsyncImage(
                    model = post.mediaUrl,
                    contentDescription = post.caption,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Caption
            if (post.caption.isNotBlank()) {
                Text(
                    text = post.caption,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Actions Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val scale by animateFloatAsState(
                    targetValue = if (post.isLiked) 1.2f else 1.0f,
                    animationSpec = spring(),
                    label = "heart_scale"
                )

                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier
                        .scale(scale)
                        .testTag("like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) AccentPink else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = post.likesCount.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier.testTag("comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = post.commentsCount.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestedUserCard(
    user: User,
    friendshipStatus: String,
    onAddClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(160.dp)
            .padding(4.dp)
            .clickable { onProfileClick() }
            .testTag("user_card_${user.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatar(
                avatarUrl = user.avatarUrl,
                displayName = user.fullName,
                size = 64.dp,
                isOnline = user.isOnline,
                hasStoryRing = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = user.fullName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "@${user.username}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))

            when (friendshipStatus) {
                "friends" -> {
                    OutlinedButton(
                        onClick = onProfileClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("Friends", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                "pending_sent" -> {
                    Button(
                        onClick = onAddClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Text("Sent", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                }
                else -> {
                    Button(
                        onClick = onAddClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("add_friend_button_${user.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
