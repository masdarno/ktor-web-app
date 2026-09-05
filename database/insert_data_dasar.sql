insert into roles (nama) values
('Super Admin'), ('Admin'), ('Operator');

insert into genders (nama) values
('Laki-laki'), ('Perempuan');

insert into units (nama) values
('Unit Induk'), ('Unit Cabang');

insert into menus (type, nama) values ('title', 'Master');
insert into menus (type, nama, url, icon) values ('group', 'Pengguna', '#', 'cil-star');
set @grup_pengguna = LAST_INSERT_ID();
insert into menus (parent_id, type, nama, url, icon) values (@grup_pengguna, 'item', 'Daftar Pengguna', '/users', 'cil-user');
insert into menus (parent_id, type, nama, url, icon) values (@grup_pengguna, 'item', 'Ubah Password', '/change-password', 'cil-user');

insert into role_menus (role_id, menu_id)
select a.id, b.id
from roles a, menus b;