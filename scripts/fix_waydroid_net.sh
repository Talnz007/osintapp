#!/usr/bin/env bash
set -e

echo "=== Fixing Waydroid Network & Routing ==="

# 1. Add iptables rules to bypass Docker's FORWARD DROP policy
sudo iptables -I FORWARD 1 -i waydroid0 -j ACCEPT
sudo iptables -I FORWARD 1 -o waydroid0 -m state --state RELATED,ESTABLISHED -j ACCEPT
sudo iptables -t nat -I POSTROUTING 1 -s 192.168.240.0/24 ! -d 192.168.240.0/24 -j MASQUERADE

# 2. Restart waydroid-container to load waydroid.cfg with Google DNS & unfreeze
echo "Restarting waydroid-container..."
sudo systemctl restart waydroid-container

# Wait for container initialization
sleep 2

# 3. Explicitly set DNS inside Android property space
sudo waydroid shell setprop net.dns1 8.8.8.8
sudo waydroid shell setprop net.dns2 1.1.1.1

echo ""
echo "Waydroid internet routing and DNS are now active!"
echo "Now start your session:"
echo "  waydroid session start"
