use `ktor-web-app`;

insert into roles (nama) values
('Super Admin'), ('Admin'), ('Operator');

insert into genders (nama) values
('Laki-laki'), ('Perempuan');

insert into company_profiles (
    nama_pemerintah,
    nama_perusahaan,
    nama_singkat,
    alamat,
    logo_kiri,
    logo_kanan
) values (
    'PEMERINTAH KABUPATEN BOYOLALI',
    'PERUSAHAAN UMUM DAERAH AIR MINUM',
    'TIRTA AMPERA',
    'Komplek Perkantoran Alun-alun Lor Kragilan Mojosongo Boyolali 57316',
    'logo-pemkab-boyolali.jpg',
    'logo-perumda-boyolali.png'
);

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

-- User Superadmin
insert into users(nama, alias, username, password, role_id, email, email_verified_at)
values ('Superadmin', 'Super', 'super', '$2a$10$mpfckf1HQdfMep4eiSz8teQzhf/NCHi6S9OgVil/Tki99nY9ilpt.', 1, 'super@mail.com', current_timestamp);

insert into user_units (user_id, unit_id)
select last_insert_id(), id
from units;

-- ==========================================================================================
-- WILAYAH
-- ==========================================================================================
insert into provinsi(kode, nama)
select id, name
from wilayah_indonesia.reg_provinces;

insert into kabupaten (provinsi_id, kode, nama)
select b.id provinsi_id, a.id kode, a.name nama
from wilayah_indonesia.reg_regencies a
join provinsi b on a.province_id  = b.kode;

insert into kecamatan (kabupaten_id, kode, nama)
select b.id, a.id, a.name
from wilayah_indonesia.reg_districts a
join kabupaten b on a.regency_id = b.kode;

insert into kelurahan (kecamatan_id, kode, nama)
select b.id, a.id, a.name
from wilayah_indonesia.reg_villages a
join kecamatan b on a.district_id = b.kode;