DESCRIPTION = "Miscellaneous files for the base system."
LICENSE = "GPL"

RECIPE_TYPES = "machine"

RDEPENDS_${PN} = "libnss-files libnss-dns"

RECIPE_FLAGS += "hostname"
DEFAULT_USE_hostname = "oe-lite"

RECIPE_FLAGS += "basefiles_version"

SRC_URI = "\
	file://motd \
	file://host.conf \
	file://profile \
	file://fstab \
	file://dot.bashrc \
	file://device_table-minimal.txt \
	file://nsswitch.conf \
	file://dot.profile \
"

RECIPE_FLAGS += "basefiles_issue"
DEFAULT_USE_basefiles_issue = "OE Lite Linux"

do_compile[dirs] = "${SRCDIR}"
do_compile () {
    echo ${USE_hostname}		> hostname
    echo "${USE_basefiles_issue} \n \l"	> issue
    echo				>>issue
    echo "${USE_basefiles_issue} %h"	> issue.net
    echo				>>issue.net
}

# Basic filesystem directories (adheres to FHS)
dirs1777 = "/tmp \
	   ${localstatedir}/lock \
	   ${localstatedir}/tmp"
dirs2775 = "/home \
	    ${localstatedir}/local"
dirs755 = "${bindir} \
	   ${sbindir} \
	   ${libdir} \
	   ${libexecdir} \
	   ${includedir} \
	   ${sysconfdir} \
	   ${sysconfdir}/default \
	   ${sysconfdir}/skel \
	   ${prefix} \
	   ${docdir} \
	   ${datadir} \
	   ${infodir} \
	   ${mandir} \
	   ${datadir}/misc \
	   ${localstatedir} \
	   ${localstatedir}/backups \
	   ${localstatedir}/lib \
	   ${localstatedir}/lib/misc \
	   ${localstatedir}/spool \
	   ${localstatedir}/cache \
	   ${localstatedir}/log \
	   ${localstatedir}/run \
	   /sys \
	   /boot \
	   /dev \
	   /dev/pts \
	   /dev/shm \
	   /mnt \
	   /proc \
	   /root \
	   /srv \
	   /media \
	   /media/card \
	   /media/cf \
	   /media/net \
	   /media/ram \
	   /media/hdd "

FILES_${PN} = "/"

do_install () {
	# Install directories
	for d in ${dirs755}; do
		install -m 0755 -d ${D}$d
	done
	for d in ${dirs1777}; do
		install -m 1777 -d ${D}$d
	done
	for d in ${dirs2775}; do
		install -m 2755 -d ${D}$d
	done

	# Install files
        install -m 0644 ${SRCDIR}/hostname ${D}${sysconfdir}/hostname
 	install -m 0644 ${SRCDIR}/issue ${D}${sysconfdir}/issue
        install -m 0644 ${SRCDIR}/issue.net ${D}${sysconfdir}/issue.net
        install -m 0644 ${SRCDIR}/fstab ${D}${sysconfdir}/fstab
        install -m 0644 ${SRCDIR}/profile ${D}${sysconfdir}/profile
        install -m 0755 ${SRCDIR}/dot.profile ${D}${sysconfdir}/skel/.profile
        install -m 0755 ${SRCDIR}/dot.bashrc ${D}${sysconfdir}/skel/.bashrc
        install -m 0644 ${SRCDIR}/motd ${D}${sysconfdir}/motd
        install -m 0644 ${SRCDIR}/host.conf ${D}${sysconfdir}/host.conf
        install -m 0644 ${SRCDIR}/nsswitch.conf ${D}${sysconfdir}/

        ln -sf /proc/mounts ${D}${sysconfdir}/mtab
}

POSTFUNCS = ""
POSTFUNCS:>USE_basefiles_version = "do_install_basefiles_version"
do_install[postfuncs] += "${POSTFUNCS}"
do_install_basefiles_version () {
	echo "${DISTRO_VERSION}" > ${SRCDIR}/${USE_basefiles_version}
	install -m 0644 ${SRCDIR}/${USE_basefiles_version} \
		${D}${sysconfdir}/${USE_basefiles_version}
}
do_install_basefiles_version[expand] = 2

inherit makedevs
MAKEDEVS_FILES = "${SRCDIR}/device_table-minimal.txt"
