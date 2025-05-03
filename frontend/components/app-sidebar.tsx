import * as React from "react"
import { ChevronRight, File, Folder } from "lucide-react"

import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible"
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuBadge,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarMenuSub,
  SidebarRail,
} from "@/components/ui/sidebar"
import {DirectoryIcon} from "@/components/icons/directory-icons";

// This is sample data.
const data = {
  changes: [
    {
      file: "README.md",
      state: "M",
    },
    {
      file: "api/hello/route.ts",
      state: "U",
    },
    {
      file: "app/layout.tsx",
      state: "M",
    },
  ],
  tree: [
    [
      "app",
      [
        "api",
        ["hello", ["route.ts"]],
        "page.tsx",
        "layout.tsx",
        ["blog", ["page.tsx"]],
      ],
    ],
    [
      "components",
      ["ui", "button.tsx", "card.tsx"],
      "header.tsx",
      "footer.tsx",
    ],
    ["lib", ["util.ts"]],
    ["public", "favicon.ico", "vercel.svg"],
    ".eslintrc.json",
    ".gitignore",
    "next.config.js",
    "tailwind.config.js",
    "package.json",
    "README.md",
  ],
}

function getFileType(filename: string): string {
  if (!filename.includes('.')) return 'folder'

  const extension = filename.split('.').pop()?.toLowerCase() || ''

  const typeMap: { [key: string]: string } = {
    ts: 'typescript',
    tsx: 'typescript',
    js: 'javascript',
    jsx: 'javascript',
    md: 'document',
    json: 'javascript',
    css: 'css',
    html: 'html5',
    py: 'python',
    java: 'java',
    sh: 'sh',
    yaml: 'yaml',
    yml: 'yaml',
    xml: 'xml',
    svg: 'imageFile',
    png: 'imageFile',
    jpg: 'imageFile',
    jpeg: 'imageFile',
    gif: 'imageFile',
    ico: 'imageFile',
  }

  return typeMap[extension] || 'file'
}

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  return (
      <Sidebar {...props}>
        <SidebarContent>
          <SidebarGroup>
            <SidebarGroupLabel>Changes</SidebarGroupLabel>
            <SidebarGroupContent>
              <SidebarMenu>
                {data.changes.map((item, index) => (
                    <SidebarMenuItem key={index}>
                      <SidebarMenuButton>
                        <DirectoryIcon name={getFileType(item.file)} />
                        {item.file}
                      </SidebarMenuButton>
                      <SidebarMenuBadge>{item.state}</SidebarMenuBadge>
                    </SidebarMenuItem>
                ))}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
          <SidebarGroup>
            <SidebarGroupLabel>Files</SidebarGroupLabel>
            <SidebarGroupContent>
              <SidebarMenu>
                {data.tree.map((item, index) => (
                    <Tree key={index} item={item} />
                ))}
              </SidebarMenu>
            </SidebarGroupContent>
          </SidebarGroup>
        </SidebarContent>
        <SidebarRail />
      </Sidebar>
  )
}

function Tree({ item }: { item: string | any[] }) {
  const [name, ...items] = Array.isArray(item) ? item : [item]

  if (!items.length) {
    return (
        <SidebarMenuButton
            isActive={name === "button.tsx"}
            className="data-[active=true]:bg-transparent"
        >
          <DirectoryIcon name={getFileType(name)} />
          {name}
        </SidebarMenuButton>
    )
  }

  return (
      <SidebarMenuItem>
        <Collapsible
            className="group/collapsible [&[data-state=open]>button>svg:first-child]:rotate-90"
            defaultOpen={name === "components" || name === "ui"}
        >
          <CollapsibleTrigger asChild>
            <SidebarMenuButton>
              <ChevronRight className="transition-transform" />
              <DirectoryIcon name={items.length ? "folder" : getFileType(name)} />
              {name}
            </SidebarMenuButton>
          </CollapsibleTrigger>
          <CollapsibleContent>
            <SidebarMenuSub>
              {items.map((subItem, index) => (
                  <Tree key={index} item={subItem} />
              ))}
            </SidebarMenuSub>
          </CollapsibleContent>
        </Collapsible>
      </SidebarMenuItem>
  )
}
