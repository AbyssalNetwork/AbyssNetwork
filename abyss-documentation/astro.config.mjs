// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// https://astro.build/config
export default defineConfig({
	integrations: [
		starlight({
			title: 'AbyssNetwork',
			social: [{ icon: 'github', label: 'GitHub', href: 'https://github.com/AbyssalNetwork' }],
			sidebar: [
				{
					label: 'Guides',
					items: [
						// Each item here is one entry in the navigation menu.
						{ label: 'Getting Started', slug: 'guides/gettingstarted' },
						{ label: 'Help Command', slug: 'guides/helpcommand' },
						{ label: 'Staff Help Command', slug: 'guides/staffhelpcommand' },
						{ label: 'Weapon Changer Command', slug: 'guides/weaponchangercommand' },
					],
				},
				{
					label: 'Reference',
					items: [{ autogenerate: { directory: 'reference' } }],
				},
			],
		}),
	],
});
