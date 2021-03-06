# -*- mode:python; -*-
DESCRIPTION = "Das U-Boot bootloader native tools"
HOMEPAGE ?= "http://www.denx.de/wiki/U-Boot"

RECIPE_TYPES = "machine native sdk"

COMPATIBLE_HOST_ARCHS = ".*linux"

inherit c make

SRC_URI = "http://ftp.denx.de/pub/u-boot/u-boot-${PV}.tar.bz2"
S = "${SRCDIR}/u-boot-${PV}"
MIRRORS =. """http://ftp.denx.de/pub/u-boot/ ftp://ftp.denx.de/pub/u-boot/
"""

EXTRA_OEMAKE = 'HOSTCC="${CC}" \
    HOSTLDFLAGS="${HOST_LDFLAGS}"'

EXTRA_OEMAKE:machine = ' \
    LDFLAGS="${LDFLAGS}" \
    CROSS_COMPILE="${HOST_PREFIX}"'

RECIPE_FLAGS = "u_boot_tools_config"
DEFAULT_USE_u_boot_tools_config = "defconfig"

do_configure () {
    oe_runmake ${USE_u_boot_tools_config}
}

MAKE_TARGETS = "tools-all"
MAKE_TARGETS:machine = "cross_tools env"

do_compile[prefuncs] += "do_compile_fix_gdb_include_path"

do_compile_fix_gdb_include_path() {
    sed 's#\(BFD_ROOT_DIR =\).*#\1 ${HOST_SYSROOT}${includedir}#' -i ${S}/tools/gdb/Makefile
}

do_compile () {
    oe_runmake ${MAKE_TARGETS}
}

inherit auto-package-utils
U_BOOT_TOOLS = "bmp_logo mkimage envcrc gen_eth_addr img2srec img2brec.sh \
    jtagconsole netconsole setlocalversion"

GDB_TOOLS:machine = ""
GDB_TOOLS = "install -m 0755 tools/gdb/gdbcont ${D}${bindir}; \
             install -m 0755 tools/gdb/gdbsend ${D}${bindir}"

do_install () {
    install -d ${D}${bindir}
    for tool in ${U_BOOT_TOOLS} ; do
        if [ -f tools/$tool${HOST_EXEEXT} ] ; then
            install -m 0755 tools/$tool${HOST_EXEEXT} \
                ${D}${bindir}/$tool${HOST_EXEEXT}
        fi
    done

    ${GDB_TOOLS}

    if [ -f tools/env/fw_printenv${HOST_EXEEXT} ] ; then
        install -m 0755 tools/env/fw_printenv${HOST_EXEEXT} \
            ${D}${bindir}/fw_printenv${HOST_EXEEXT}
    fi
    ln -s fw_printenv${HOST_EXEEXT} ${D}${bindir}/fw_setenv${HOST_EXEEXT}
}

inherit auto-package-utils
AUTO_PACKAGE_UTILS = "${U_BOOT_TOOLS}"

PACKAGES =+ "${PN}-env"
FILES_${PN}-env = "${bindir}/fw_*env"
PROVIDES_${PN}-env = "util/fw_printenv util/fw_setenv"

DEPENDS_${PN}-mkimage += "dtc"
RDEPENDS_${PN}-mkimage += "dtc"

PACKAGES =+ "${PN}-gdb"
FILES_${PN}-gdb = "${bindir}/gdb*"
