#!/usr/bin/env python3
"""Fail closed on common Play packaging mistakes; does not certify Play approval."""
import argparse
import json
import re
import struct
import subprocess
import zipfile

parser = argparse.ArgumentParser()
parser.add_argument('apk')
parser.add_argument('--aapt2', required=True)
args = parser.parse_args()
badging = subprocess.check_output([args.aapt2, 'dump', 'badging', args.apk], text=True)
assert "name='com.emerytech.handheldruntime'" in badging, 'Wrong distribution package'
assert 'application-debuggable' not in badging, 'Debug APK cannot be submitted'
permissions = set(re.findall(r"uses-permission: name='([^']+)'", badging))
forbidden = {'android.permission.' + name for name in [
    'MANAGE_EXTERNAL_STORAGE', 'READ_EXTERNAL_STORAGE', 'WRITE_EXTERNAL_STORAGE',
    'REQUEST_INSTALL_PACKAGES', 'READ_LOGS',
]}
assert not permissions & forbidden, sorted(permissions & forbidden)
checked = 0
with zipfile.ZipFile(args.apk) as archive:
    names = archive.namelist()
    assert 'assets/libredirect-bionic-wx.so' in names, 'Missing modern runtime asset'
    for required in ['libextras.so', 'libwinlator.so', 'libvulkan_renderer.so']:
        assert 'lib/arm64-v8a/' + required in names, f'Missing native runtime: {required}'
    assert not any(name.lower().endswith(('lossless.dll', '.lsfgcache', 'login.txt', 'battle_net_login.xml', '.keystore', '.p12')) for name in names), 'Private or proprietary asset found'
    for name in names:
        if not (name.startswith('lib/') and name.endswith('.so')):
            continue
        blob = archive.read(name)
        assert blob[:6] == b'\x7fELF\x02\x01', f'Expected little-endian ELF64: {name}'
        offset = struct.unpack_from('<Q', blob, 32)[0]
        entry_size, count = struct.unpack_from('<HH', blob, 54)
        alignments = [struct.unpack_from('<Q', blob, offset + i * entry_size + 48)[0]
                      for i in range(count) if struct.unpack_from('<I', blob, offset + i * entry_size)[0] == 1]
        assert alignments and all(alignment >= 16384 for alignment in alignments), f'Needs 16 KB relink: {name}'
        checked += 1
assert checked, 'No native libraries checked'
print(json.dumps({'package': 'com.emerytech.handheldruntime', 'native_libraries_16kb_aligned': checked,
                  'forbidden_permissions': [], 'debuggable': False, 'private_asset_paths': []}))
