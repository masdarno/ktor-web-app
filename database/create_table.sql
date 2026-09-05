use `ktor-web-app`;

-- ==========================================================================================
-- AUTH
-- ==========================================================================================
create table `roles` (
  `id` tinyint unsigned auto_increment,
  `nama` varchar(14) not null,
  `is_active` tinyint unsigned not null default 1 check (is_active in (0, 1)),
  `created_at` datetime default current_timestamp(),
  `updated_at` datetime default null on update current_timestamp(),
  primary key (`id`)
);

create table `menus` (
  `id` smallint unsigned auto_increment,
  `parent_id` smallint unsigned comment 'ID menu induk',
  `type` enum('item','title','group','divider') not null default 'item' comment 'Tipe menu: item, title, group, atau divider',
  `nama` varchar(255) comment 'Nama menu',
  `url` varchar(255) comment 'URL untuk tautan',
  `icon` varchar(255) comment 'Nama ikon (misal: cil-speedometer)',
  `badge_text` varchar(255) comment 'Teks badge (misal: PRO, New)',
  `badge_color` varchar(255) comment 'Kelas warna badge (misal: bg-danger-gradient)',
  `urut` int(11) NOT NULL DEFAULT 0 COMMENT 'Urutan menu',
  `permission_name` varchar(255) comment 'Nama permission untuk role-based access control',
  primary key (`id`),
  foreign key (`parent_id`) references `menus` (`id`) on delete set null on update cascade
);

create table `role_menus` (
  `role_id` tinyint unsigned not null,
  `menu_id` smallint unsigned not null,
  primary key (`menu_id`,`role_id`),
  foreign key (`role_id`) references `roles` (`id`) on update cascade,
  foreign key (`menu_id`) references `menus` (`id`) on update cascade
);

create table `genders` (
  `id` tinyint unsigned auto_increment,
  `nama` varchar(50) not null default '' unique,
  `is_active` tinyint unsigned not null default 1 check (is_active in (0, 1)),
  `created_at` datetime default current_timestamp(),
  `updated_at` datetime default null on update current_timestamp(),
  primary key (`id`)
);

create table `users` (
  `id` tinyint unsigned auto_increment,
  `nama` varchar(60) not null,
  `alias` varchar(50) not null default '',
  `username` varchar(10) not null unique,
  `password` char(60) not null,
  `gender_id` tinyint unsigned not null default 2,
  `photo` varchar(100) not null default 'male.jpg',
  `role_id` tinyint unsigned not null,
  `email` varchar(50),
  `email_verified_at` timestamp,
  `is_active` tinyint unsigned not null default 1 check (is_active in (0, 1)),
  `created_at` datetime default current_timestamp(),
  `updated_at` datetime default null on update current_timestamp(),
  primary key (`id`),
  foreign key (`role_id`) references `roles` (`id`) on update cascade,
  foreign key (`gender_id`) references `genders` (`id`) on update cascade
);

create table `units` (
  `id` tinyint unsigned auto_increment,
  `nama` varchar(50) not null default '',
  `is_active` tinyint unsigned not null default 1 check (is_active in (0, 1)),
  `created_at` datetime default current_timestamp(),
  `updated_at` datetime default null on update current_timestamp(),
  primary key (`id`)
);

create table `user_units` (
  `user_id` tinyint unsigned not null,
  `unit_id` tinyint unsigned not null,
  primary key (`user_id`,`unit_id`),
  foreign key (`user_id`) references `users` (`id`),
  foreign key (`unit_id`) references `units` (`id`)
);

create table `email_verification_tokens` (
  `token` varchar(255) not null,
  `user_id` tinyint(3) unsigned not null,
  `expires_at` datetime not null,
  primary key (`token`),
  foreign key (`user_id`) references `users` (`id`)
);

create table `password_reset_tokens` (
  `token` varchar(64) not null,
  `user_id` tinyint(3) unsigned not null,
  `expires_at` datetime not null,
  `used_at` datetime,
  primary key (`token`),
  foreign key (`user_id`) references `users` (`id`)
);

create table `remember_me_tokens` (
  `selector` varchar(64) NOT NULL,
  `user_id` tinyint(3) unsigned NOT NULL,
  `unit_id` tinyint(3) unsigned NOT NULL,
  `validator_hash` varchar(255) NOT NULL,
  `expires_at` datetime NOT NULL,
  primary key (`selector`),
  foreign key (`user_id`) references `users` (`id`),
  foreign key (`unit_id`) references `units` (`id`)
);
