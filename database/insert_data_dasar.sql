use `ktor-web-app`;

insert into roles (nama) values
('Super Admin'), ('Admin'), ('Operator');

insert into genders (nama) values
('Laki-laki'), ('Perempuan');

insert into units (nama) values
('Unit Induk'), ('Unit Cabang');

insert into menus (type, nama) values ('title', 'Master');
set @master = LAST_INSERT_ID();
insert into menus (type, nama, url, icon) values ('group', 'Pengguna', '#', 'cil-star');
set @master_pengguna = LAST_INSERT_ID();
insert into menus (parent_id, type, nama, url, icon) values (@master_pengguna, 'item', 'Daftar Pengguna', '/users', 'cil-user');
set @daftar_pengguna = LAST_INSERT_ID();
insert into menus (parent_id, type, nama, url, icon) values (@master_pengguna, 'item', 'Pengguna Unit', '/user-unit', 'cil-user');
set @pengguna_unit = LAST_INSERT_ID();
insert into menus (parent_id, type, nama, url, icon) values (@master_pengguna, 'item', 'Ubah Password', '/change-password', 'cil-user');
set @ubah_password = LAST_INSERT_ID();

-- menu SuperAdmin & Admin
insert into role_menus (role_id, menu_id)
select a.id, b.id
from roles a, menus b
where a.id in (1, 2);

-- menu Operator
insert into role_menus (role_id, menu_id)
select a.id, b.id
from roles a, menus b
where a.id in (3)
and b.id not in (@daftar_pengguna, @pengguna_unit);